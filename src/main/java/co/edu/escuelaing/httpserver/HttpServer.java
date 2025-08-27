package co.edu.escuelaing.httpserver;

import co.edu.escuelaing.microspringboot.annotations.GetMapping;
import co.edu.escuelaing.microspringboot.annotations.RequestParam;
import co.edu.escuelaing.microspringboot.annotations.RestController;
import java.net.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    public static Map<String, Method> services = new HashMap();

    public static void loadServices(String[] args) throws ClassNotFoundException{
        Class c = Class.forName(args[0]);
        if(c.isAnnotationPresent(RestController.class)){
        
            Method[] methods = c.getDeclaredMethods();
            for(Method m : methods){
                if(m.isAnnotationPresent(GetMapping.class)){
                    String mapping = m.getAnnotation(GetMapping.class).value();
                    services.put(mapping, m);
                }
            }
            
        }      
        
    }
    
    public static void runServer(String[] args) throws IOException, URISyntaxException, IllegalAccessException, InvocationTargetException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        Socket clientSocket = null;

        boolean running = true;
        while (running) {
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;

            String path = null;
            boolean firstline = true;
            URI requri = null;

            while ((inputLine = in.readLine()) != null) {
                if (firstline) {
                    requri = new URI(inputLine.split(" ")[1]);
                    System.out.println("Path: " + requri.getPath());
                    firstline = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            if (requri.getPath().startsWith("/app")) {
                outputLine = invokeService(requri);
            } else {
                //Leo del disco

                outputLine = defaultResponse();
            }
            out.println(outputLine);

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String invokeService(URI requri) throws IllegalAccessException, InvocationTargetException {

        String header = "HTTP/1.1 200 OK\n\r"
                + "content-type: text/html\n\r"
                + "\n\r";
        
        HttpRequest req = new HttpRequest(requri);
        HttpResponse res = new HttpResponse();
        String servicePath = requri.getPath().substring(4);
        Method m = services.get(servicePath);
        
        String[] argsValues = null;
        RequestParam rp = (RequestParam) m.getParameterAnnotations()[0][0];
        
        if(requri.getQuery() == null){
            
            argsValues = new String[]{rp.defaultValue()};
            
        } else {
        
            String queryParamName = rp.value();
            argsValues = new String[]{req.getValue(queryParamName)};
            
        }
        
        return header + m.invoke(null, argsValues);
    }

    public static void staticfiles(String localFilesPath) {
    }

    public static void start(String[] args) throws IOException, URISyntaxException, IllegalAccessException, InvocationTargetException {
        runServer(args);
    }

    public static String defaultResponse() {
        return "HTTP/1.1 200 OK\r\n"
                + "content-type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<title>Form Example</title>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "</head>\n"
                + "<body>\n"
                + "<h1>Form with GET</h1>\n"
                + "<form action=\"/hello\">\n"
                + "<label for=\"name\">Name:</label><br>\n"
                + "<input type=\"text\" id=\"name\" name=\"name\" value=\"John\"><br><br>\n"
                + "<input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n"
                + "</form>\n"
                + "<div id=\"getrespmsg\"></div>\n"
                + " \n"
                + "<script>\n"
                + "function loadGetMsg() {\n"
                + "let nameVar = document.getElementById(\"name\").value;\n"
                + "const xhttp = new XMLHttpRequest();\n"
                + "xhttp.onload = function() {\n"
                + "document.getElementById(\"getrespmsg\").innerHTML =\n"
                + "this.responseText;\n"
                + "}\n"
                + "xhttp.open(\"GET\", \"/app/hello?name=\"+nameVar);\n"
                + "xhttp.send();\n"
                + "}\n"
                + "</script>\n"
                + " \n"
                + "<h1>Form with POST</h1>\n"
                + "<form action=\"/hellopost\">\n"
                + "<label for=\"postname\">Name:</label><br>\n"
                + "<input type=\"text\" id=\"postname\" name=\"name\" value=\"John\"><br><br>\n"
                + "<input type=\"button\" value=\"Submit\" onclick=\"loadPostMsg(postname)\">\n"
                + "</form>\n"
                + " \n"
                + "<div id=\"postrespmsg\"></div>\n"
                + " \n"
                + "<script>\n"
                + "function loadPostMsg(name){\n"
                + "let url = \"/hellopost?name=\" + name.value;\n"
                + " \n"
                + "fetch (url, {method: 'POST'})\n"
                + ".then(x => x.text())\n"
                + ".then(y => document.getElementById(\"postrespmsg\").innerHTML = y);\n"
                + "}\n"
                + "</script>\n"
                + "</body>\n"
                + "</html>";
    }

}
