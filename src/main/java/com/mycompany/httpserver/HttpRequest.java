package com.mycompany.httpserver;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Wrapper simple para leer parámetros de query. */
public class HttpRequest {

    private final URI rawUri;

    public HttpRequest(URI uri) {
        this.rawUri = uri;
    }

    /** Retorna el valor de un parámetro (?name=Pedro) */
    public String getValues(String paramName) {
        Map<String, String> all = getQueryMap();
        return all.getOrDefault(paramName, "");
    }

    /** Alias opcional (por si lo usan en pruebas) */
    public String getValue(String paramName) {
        return getValues(paramName);
    }

    /** Mapa completo de parámetros de query. */
    public Map<String, String> getQueryMap() {
        String q = rawUri.getRawQuery();
        Map<String, String> map = new HashMap<>();
        if (q == null || q.isEmpty()) return map;

        for (String kv : q.split("&")) {
            int i = kv.indexOf('=');
            String k = i >= 0 ? kv.substring(0, i) : kv;
            String v = i >= 0 ? kv.substring(i + 1) : "";
            k = urlDecode(k);
            v = urlDecode(v);
            map.put(k, v);
        }
        return map;
    }

    private String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
