package com.mycompany.microframework.core;

import com.mycompany.httpserver.HttpResponse;
import com.mycompany.httpserver.HttpServer;
import com.mycompany.httpserver.HttpRequest;
import microframework.annotations.GetMapping;
import microframework.annotations.RequestParam;


import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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
            GetMapping gm = m.getAnnotation(GetMapping.class);
            if (gm == null) continue;
            if (!m.getReturnType().equals(String.class)) {
                throw new IllegalArgumentException("@GetMapping solo admite retorno String: " + m);
            }
            m.setAccessible(true);
            String path = normalize(gm.value());
            routes.put(path, new MethodBinding(controller, m));

            // Conecta con tu HttpServer existente
            HttpServer.get(path, (HttpRequest req, HttpResponse resp) -> invoke(path, req, resp));
        }
    }

    private String invoke(String path, HttpRequest req, HttpResponse resp) {
        MethodBinding b = routes.get(path);
        if (b == null) return "404 Not Found";

        Object[] args = new Object[b.params.length];
        Map<String, String> query = parseQueryString(safePath(req));

        for (int i = 0; i < b.params.length; i++) {
            Parameter p = b.params[i];
            if (p.getType().equals(HttpRequest.class)) {
                args[i] = req;
            } else if (p.getType().equals(HttpResponse.class)) {
                args[i] = resp;
            } else {
                RequestParam rp = p.getAnnotation(RequestParam.class);
                if (rp == null) {
                    // Solo soportamos parámetros con @RequestParam (además de HttpRequest/HttpResponse)
                    args[i] = null;
                } else {
                    String name = rp.value();
                    String val = query.getOrDefault(name, rp.defaultValue());
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

    private static String normalize(String p) {
        if (p == null || p.isEmpty()) return "/";
        if (!p.startsWith("/")) return "/" + p;
        return p;
    }

    /** Intenta obtener la ruta completa del request */
    private static String safePath(HttpRequest req) {
        try {
            // Implementación común en esta kata
            Method m = req.getClass().getMethod("getPath");
            Object v = m.invoke(req);
            if (v != null) return v.toString();
        } catch (Exception ignored) {}
        return "/";
    }

    private static Map<String, String> parseQueryString(String path) {
        int q = path.indexOf('?');
        Map<String, String> map = new HashMap<>();
        if (q < 0 || q == path.length() - 1) return map;
        String[] pairs = path.substring(q + 1).split("&");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String k = urlDecode(pair.substring(0, eq));
                String v = urlDecode(pair.substring(eq + 1));
                map.put(k, v);
            } else if (!pair.isEmpty()) {
                map.put(urlDecode(pair), "");
            }
        }
        return map;
    }

    private static String urlDecode(String s) {
        try { return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8.name()); }
        catch (Exception e){ return s; }
    }

    private static Object convert(String v, Class<?> target) {
        if (target.equals(String.class)) return v;
        if (target.equals(int.class) || target.equals(Integer.class)) return (v==null||v.isEmpty())?0:Integer.parseInt(v);
        if (target.equals(long.class) || target.equals(Long.class)) return (v==null||v.isEmpty())?0L:Long.parseLong(v);
        if (target.equals(boolean.class) || target.equals(Boolean.class)) return Boolean.parseBoolean(v);
        // Se pueden añadir más conversiones si lo deseas
        return v;
    }
}
