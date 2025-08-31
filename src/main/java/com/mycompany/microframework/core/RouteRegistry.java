package com.mycompany.microframework.core;

import com.mycompany.httpserver.HttpRequest;
import com.mycompany.httpserver.HttpResponse;
import com.mycompany.httpserver.HttpServer;


import java.lang.reflect.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RouteRegistry {

    private final Map<String, MethodBinding> routes = new ConcurrentHashMap<>();

    private static class MethodBinding {
        final Object instance;
        final Method method;
        final Parameter[] params;
        MethodBinding(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
            this.params = method.getParameters();
        }
    }

    public void register(Object controller) {
        for (Method m : controller.getClass().getDeclaredMethods()) {
            microframework.annotations.GetMapping gm = m.getAnnotation(microframework.annotations.GetMapping.class);
            if (gm == null) continue;
            if (!m.getReturnType().equals(String.class)) {
                throw new IllegalArgumentException("@GetMapping solo admite retorno String: " + m);
            }
            m.setAccessible(true);
            String path = normalize(gm.value());
            routes.put(path, new MethodBinding(controller, m));

            System.out.println("[ioc] map GET " + path + " -> " +
                    controller.getClass().getName() + "#" + m.getName());

            HttpServer.get(path, (HttpRequest req, HttpResponse resp) -> invoke(path, req, resp));
            if (!path.endsWith("/")) {
                HttpServer.get(path + "/", (HttpRequest req, HttpResponse resp) -> invoke(path, req, resp));
            }
        }
    }

    private String invoke(String path, HttpRequest req, HttpResponse resp) {
        MethodBinding b = routes.get(path);
        if (b == null) return "404 Not Found";

        Object[] args = new Object[b.params.length];
        Map<String, String> query = extractQueryMap(req); // mapa si existe

        for (int i = 0; i < b.params.length; i++) {
            Parameter p = b.params[i];
            if (p.getType().equals(HttpRequest.class)) {
                args[i] = req;
            } else if (p.getType().equals(HttpResponse.class)) {
                args[i] = resp;
            } else {
                microframework.annotations.RequestParam rp = p.getAnnotation(microframework.annotations.RequestParam.class);
                if (rp == null) {
                    args[i] = null; // solo soportamos @RequestParam + HttpRequest/HttpResponse
                } else {
                    String key = rp.value();
                    String val = query.get(key);
                    if (val == null) {
                        // fallback: métodos (String)->String tipo getQueryParam/getParam/getValue/etc.
                        val = extractSingleQueryValue(req, key);
                    }
                    if (val == null || val.isEmpty()) {
                        val = rp.defaultValue();
                    }
                    args[i] = convert(val, p.getType());
                }
            }
        }

        try {
            Object out = b.method.invoke(b.instance, args);
            return out == null ? "" : out.toString();
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
            return "500 Internal Server Error\n" + cause.getClass().getSimpleName() + ": " + cause.getMessage();
        } catch (Exception e) {
            return "500 Internal Server Error\n" + e.getMessage();
        }
    }

    /* ===================== extractores ===================== */

    private static String normalize(String p) {
        if (p == null || p.isEmpty()) return "/";
        if (!p.startsWith("/")) p = "/" + p;
        return p;
    }

    /** Intenta obtener un Map<String,String> de la request mediante varios nombres/mecanismos. */
    @SuppressWarnings("unchecked")
    private static Map<String, String> extractQueryMap(HttpRequest req) {
        // 1) Métodos que retornan Map
        for (String name : List.of("getQueryMap", "getQueryParams", "getParams", "params", "getParameters")) {
            try {
                Method m = req.getClass().getMethod(name);
                Object o = m.invoke(req);
                if (o instanceof Map) return toStringMap((Map<?, ?>) o);
            } catch (Exception ignored) {}
        }

        // 2) Campos Map con nombres típicos
        for (Field f : req.getClass().getDeclaredFields()) {
            if (!Map.class.isAssignableFrom(f.getType())) continue;
            String n = f.getName().toLowerCase(Locale.ROOT);
            if (n.contains("query") || n.contains("param")) {
                try {
                    f.setAccessible(true);
                    Object o = f.get(req);
                    if (o instanceof Map) return toStringMap((Map<?, ?>) o);
                } catch (Exception ignored) {}
            }
        }

        // 3) Strings de query
        for (String name : List.of("getQueryString", "getQuery", "queryString", "query")) {
            try {
                Method m = req.getClass().getMethod(name);
                Object o = m.invoke(req);
                if (o instanceof String) return parseQueryString((String) o);
            } catch (Exception ignored) {}
        }

        // 4) URI/target que contenga '?'
        for (String name : List.of("getRequestTarget", "getUri", "uri", "requestTarget", "getPath", "path")) {
            try {
                Method m = req.getClass().getMethod(name);
                Object o = m.invoke(req);
                if (o instanceof String) {
                    String s = (String) o;
                    int q = s.indexOf('?');
                    if (q >= 0 && q < s.length() - 1) {
                        return parseQueryString(s.substring(q + 1));
                    }
                }
            } catch (Exception ignored) {}
        }

        return Collections.emptyMap();
    }

    /** Si no hay Map, intenta métodos que acepten (String) y retornen el valor del query param. */
    private static String extractSingleQueryValue(HttpRequest req, String key) {
        // (String)->String típicos
        for (String name : List.of(
                "getQueryParam", "getQueryParameter", "getParameter", "getParam",
                "queryParam", "param", "getValue", "get"
        )) {
            try {
                Method m = req.getClass().getMethod(name, String.class);
                Object o = m.invoke(req, key);
                if (o != null) return String.valueOf(o);
            } catch (Exception ignored) {}
        }
        // (String)->List<String> o String[]
        for (String name : List.of("getParameterValues", "getParamValues", "queryParams")) {
            try {
                Method m = req.getClass().getMethod(name, String.class);
                Object o = m.invoke(req, key);
                if (o instanceof List && !((List<?>) o).isEmpty()) {
                    Object first = ((List<?>) o).get(0);
                    return first == null ? "" : String.valueOf(first);
                }
                if (o instanceof String[] arr && arr.length > 0) {
                    return arr[0];
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Map<String, String> toStringMap(Map<?, ?> in) {
        Map<String, String> out = new HashMap<>();
        for (Map.Entry<?, ?> e : in.entrySet()) {
            String k = String.valueOf(e.getKey());
            Object v = e.getValue();
            if (v == null) {
                out.put(k, "");
            } else if (v instanceof List && !((List<?>) v).isEmpty()) {
                out.put(k, String.valueOf(((List<?>) v).get(0)));
            } else if (v.getClass().isArray()) {
                Object[] arr = (Object[]) v;
                out.put(k, arr.length > 0 ? String.valueOf(arr[0]) : "");
            } else {
                out.put(k, String.valueOf(v));
            }
        }
        return out;
    }

    private static Map<String, String> parseQueryString(String qs) {
        Map<String, String> map = new HashMap<>();
        if (qs == null || qs.isEmpty()) return map;
        if (qs.startsWith("?")) qs = qs.substring(1);
        for (String pair : qs.split("&")) {
            if (pair.isEmpty()) continue;
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String k = urlDecode(pair.substring(0, eq));
                String v = urlDecode(pair.substring(eq + 1));
                map.put(k, v);
            } else {
                map.put(urlDecode(pair), "");
            }
        }
        return map;
    }

    private static String urlDecode(String s) {
        try { return URLDecoder.decode(s, StandardCharsets.UTF_8.name()); }
        catch (Exception e){ return s; }
    }

    private static Object convert(String v, Class<?> target) {
        if (target.equals(String.class)) return v;
        if (target.equals(int.class) || target.equals(Integer.class)) return (v == null || v.isEmpty()) ? 0 : Integer.parseInt(v);
        if (target.equals(long.class) || target.equals(Long.class)) return (v == null || v.isEmpty()) ? 0L : Long.parseLong(v);
        if (target.equals(boolean.class) || target.equals(Boolean.class)) return Boolean.parseBoolean(v);
        return v; // fallback
    }
}
