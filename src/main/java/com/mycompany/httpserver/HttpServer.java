package com.mycompany.httpserver;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Mini framework para rutas GET con lambdas, parámetros de query y estáticos.
 * Puerto por defecto: 8080 (se puede pasar por args[0]).
 */
public class HttpServer {

    // ====== Variables RENOMBRADAS para diferenciación ======
    // Registro de endpoints (antes: services)
    private static final Map<String, Service> endpointRegistry = new HashMap<>();

    // Carpeta base de estáticos en target/classes (antes: principalPath)
    private static String assetsBasePath = "target/classes/webroot";

    // Carpeta base de origen (dev) desde resources (para copiar)
    private static String resourcesBasePath = "src/main/resources";

    // Tipos MIME
    private static final Map<String, String> MIME = new HashMap<>();
    static {
        MIME.put("html", "text/html; charset=utf-8");
        MIME.put("css",  "text/css; charset=utf-8");
        MIME.put("js",   "application/javascript; charset=utf-8");
        MIME.put("json", "application/json; charset=utf-8");
        MIME.put("png",  "image/png");
        MIME.put("jpg",  "image/jpeg");
        MIME.put("jpeg", "image/jpeg");
        MIME.put("gif",  "image/gif");
        MIME.put("ico",  "image/x-icon");
        MIME.put("svg",  "image/svg+xml; charset=utf-8");
        MIME.put("ong",  "image/png"); // por si el archivo quedó con extensión .ong
    }

    // --- normalización de rutas (case-insensitive y con '/' inicial)
    private static String normalizeRoute(String r) {
        if (r == null || r.isEmpty()) return "/";
        String s = r.trim();
        if (!s.startsWith("/")) s = "/" + s;
        return s.toLowerCase(java.util.Locale.ROOT);
    }

    /** Define una ruta GET y su lambda. */
    public static void get(String route, Service handler) {
        endpointRegistry.put(normalizeRoute(route), handler);
    }

    /**
     * Define la ruta de estáticos. Copia desde resources -> target/classes
     * Ej: staticfiles("/webroot") -> copiará a target/classes/webroot
     */
    public static void staticfiles(String staticFolder) {
        String normalized = staticFolder.startsWith("/") ? staticFolder : ("/" + staticFolder);
        assetsBasePath = "target/classes" + normalized;
        String from = resourcesBasePath + normalized;

        System.out.println("[staticfiles] Copiando estáticos:");
        System.out.println("  from: " + from);
        System.out.println("  to  : " + assetsBasePath);

        try {
            createDirectories(assetsBasePath);
            copyRecursive(Paths.get(from), Paths.get(assetsBasePath));
        } catch (IOException e) {
            System.err.println("[staticfiles] Advertencia: no se pudieron copiar los archivos: " + e.getMessage());
        }
    }

    /** Inicia el servidor (args[0] = puerto opcional) */
    public static void startServer(String[] args) throws IOException {
        int port = 8080;
        if (args != null && args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }

        try (ServerSocket listener = new ServerSocket(port)) {
            System.out.println("Servidor corriendo en http://localhost:" + port);
            while (true) {
                Socket client = listener.accept();
                handleClient(client);
            }
        }
    }

    // ================== Núcleo de atención ==================
    private static void handleClient(Socket client) {
        try (InputStream inRaw = client.getInputStream();
             OutputStream outRaw = new BufferedOutputStream(client.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(inRaw, StandardCharsets.US_ASCII))) {

            String start = in.readLine(); // "GET /ruta?x=1 HTTP/1.1"
            if (start == null || start.isEmpty()) return;

            String[] parts = start.split(" ");
            String method = parts.length > 0 ? parts[0] : "GET";
            String target = parts.length > 1 ? parts[1] : "/";
            String version = parts.length > 2 ? parts[2] : "HTTP/1.1";

            // Consumir headers (no los usamos aún)
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {}

            URI uri;
            try {
                uri = new URI(target);
            } catch (URISyntaxException e) {
                writeText(outRaw, version, 400, "Bad Request");
                return;
            }

            String path = uri.getPath();

            // 1) ¿Hay handler registrado exacto?
            Service svc = endpointRegistry.get(normalizeRoute(path));
            if ("GET".equalsIgnoreCase(method) && svc != null) {
                HttpRequest req = new HttpRequest(uri);
                HttpResponse resp = new HttpResponse();
                String payload;
                try {
                    payload = svc.handle(req, resp);
                } catch (Exception ex) {
                    payload = "Internal Server Error: " + ex.getMessage();
                    write(outRaw, version, 500, "text/plain; charset=utf-8", payload.getBytes(StandardCharsets.UTF_8));
                    return;
                }
                write(outRaw, version, 200, "application/json; charset=utf-8", payload.getBytes(StandardCharsets.UTF_8));
                return;
            }

            // 2) Servir estáticos desde assetsBasePath
            serveStatic(outRaw, version, path);

        } catch (IOException ioe) {
            System.err.println("[handleClient] " + ioe.getMessage());
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    // ================== Estáticos (binario-seguro) ==================
    private static void serveStatic(OutputStream out, String version, String reqPath) throws IOException {
        String clean = normalize(reqPath);
        if ("/".equals(clean)) clean = "/index.html";

        // target/classes/webroot + clean
        Path candidate = Paths.get(assetsBasePath + clean);
        // Fallback: si no existe, intenta servir directo desde resources (útil en dev sin compilar)
        Path devCandidate = Paths.get(resourcesBasePath + clean);

        Path file = Files.exists(candidate) ? candidate : (Files.exists(devCandidate) ? devCandidate : null);

        if (file == null || Files.isDirectory(file) || !Files.exists(file)) {
            writeText(out, version, 404, "Not Found: " + clean);
            return;
        }

        byte[] data = Files.readAllBytes(file);
        String ctype = detectContentType(file.toString());
        write(out, version, 200, ctype, data);
    }

    private static String detectContentType(String path) {
        int i = path.lastIndexOf('.');
        String ext = (i >= 0) ? path.substring(i + 1).toLowerCase() : "";
        return MIME.getOrDefault(ext, "application/octet-stream");
    }

    private static String normalize(String p) {
        String s = p.replace("\\", "/");
        s = s.replaceAll("/+", "/");
        s = s.replaceAll("\\.\\.", "");  // evita traversal
        if (!s.startsWith("/")) s = "/" + s;
        return s;
    }

    // ================== Escritura de respuestas ==================
    private static void writeText(OutputStream out, String version, int code, String text) throws IOException {
        write(out, version, code, "text/plain; charset=utf-8", text.getBytes(StandardCharsets.UTF_8));
    }

    private static void write(OutputStream out, String version, int code, String contentType, byte[] body) throws IOException {
        String status = switch (code) { case 200 -> "OK"; case 400 -> "Bad Request"; case 404 -> "Not Found"; default -> "Status"; };
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write((version + " " + code + " " + status + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
        buf.write(("Content-Type: " + contentType + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
        buf.write(("Content-Length: " + body.length + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
        buf.write(("Connection: close\r\n").getBytes(StandardCharsets.ISO_8859_1));
        buf.write(("\r\n").getBytes(StandardCharsets.ISO_8859_1));
        out.write(buf.toByteArray());
        out.write(body);
        out.flush();
    }

    // ================== Utilidades de copia ==================
    private static void createDirectories(String dir) throws IOException {
        Files.createDirectories(Paths.get(dir));
    }

    private static void copyRecursive(Path from, Path to) throws IOException {
        if (!Files.exists(from)) return;
        Files.walk(from).forEach(src -> {
            try {
                Path dest = to.resolve(from.relativize(src).toString());
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
