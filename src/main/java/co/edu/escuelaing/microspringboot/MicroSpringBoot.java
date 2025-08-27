/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package co.edu.escuelaing.microspringboot;

import co.edu.escuelaing.httpserver.HttpServer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 *
 * @author luisdanielbenavidesnavarro
 */
public class MicroSpringBoot {

    public static void main(String[] args) throws IOException, URISyntaxException, IllegalAccessException, InvocationTargetException {
        System.out.println("Hello World!");
        
        HttpServer.runServer(args);
    }
}
