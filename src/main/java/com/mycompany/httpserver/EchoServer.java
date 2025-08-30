package com.mycompany.httpserver;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class EchoServer {
    public static void main(String[] args) throws Exception {
        int port = 35000;
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("EchoServer HTTP en http://localhost:" + port);
            while (true) {
                try (Socket s = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.US_ASCII));
                     BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream())) {

                    // Lee solicitud hasta línea vacía
                    String line;
                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        System.out.println("Mensaje:" + line);
                    }

                    // Respuesta HTTP válida
                    byte[] body = "<h1>EchoServer OK</h1>".getBytes(StandardCharsets.UTF_8);
                    out.write(("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=utf-8\r\n" +
                            "Content-Length: " + body.length + "\r\n" +
                            "Connection: close\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
                    out.write(body);
                    out.flush();
                }
            }
        }
    }
}
