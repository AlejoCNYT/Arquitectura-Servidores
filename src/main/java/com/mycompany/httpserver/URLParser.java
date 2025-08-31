package com.mycompany.httpserver;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * URL/Path parser muy simple para tests y el microservidor.
 * - Acepta path o URL completa.
 * - Expone path() y params() (query string).
 *
 * Ejemplo:
 *   URLParser p = new URLParser("/stocks?symbol=IBM");
 *   p.path();              // "/stocks"
 *   p.params().get("symbol"); // "IBM"
 */
public class URLParser {

    private final String path;
    private final Map<String, String> params;

    public URLParser(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) pathOrUrl = "/";

        // Si viene con "http://..." quita esquema/host y deja solo el path+query
        String s = pathOrUrl;
        int scheme = s.indexOf("://");
        if (scheme > 0) {
            int firstSlash = s.indexOf('/', scheme + 3);
            s = (firstSlash >= 0) ? s.substring(firstSlash) : "/";
        }

        // Separa path y query
        int q = s.indexOf('?');
        String qs = null;
        String pth = s;
        if (q >= 0) {
            qs = s.substring(q + 1);
            pth = s.substring(0, q);
        }
        if (pth.isEmpty()) pth = "/";

        this.path = pth;
        this.params = parseQuery(qs);
    }

    public String path() {
        return path;
    }

    public Map<String, String> params() {
        return params;
    }

    /* ----------------- helpers ----------------- */

    private static Map<String, String> parseQuery(String qs) {
        Map<String, String> map = new LinkedHashMap<>();
        if (qs == null || qs.isEmpty()) return map;

        for (String pair : qs.split("&")) {
            if (pair.isEmpty()) continue;
            int eq = pair.indexOf('=');
            String key, val;
            if (eq >= 0) {
                key = urlDecode(pair.substring(0, eq));
                val = urlDecode(pair.substring(eq + 1));
            } else {
                key = urlDecode(pair);
                val = "";
            }
            map.put(key, val);
        }
        return map;
    }

    private static String urlDecode(String s) {
        try {
            return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return s;
        }
    }
}
