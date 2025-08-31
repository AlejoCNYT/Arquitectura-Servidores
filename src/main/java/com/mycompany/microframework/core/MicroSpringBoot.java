package com.mycompany.microframework.core;

import com.mycompany.httpserver.HttpServer;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Arranca el HttpServer y registra controladores:
 *  1) Con args: carga solo esos FQCN (versión inicial).
 *  2) Sin args: escanea el paquete base (versión final).
 * Importante: registrar rutas ANTES de startServer().
 */
public class MicroSpringBoot
{

    private static final String DEFAULT_BASE_PACKAGE = "com.mycompany.webapp";
    private static final int DEFAULT_PORT = Integer.getInteger("PORT", 35000);

    public static void main(String[] args) throws Exception {
        // Estáticos
        HttpServer.staticfiles("/static");

        // 1) Registrar rutas primero
        RouteRegistry registry = new RouteRegistry();

        if (args != null && args.length > 0) {
            // Versión inicial: POJOs por argumento
            for (String fqcn : args) {
                Object instance = newControllerInstance(fqcn);
                ensureRestController(instance.getClass());
                registry.register(instance);
                System.out.println("[ioc] GET routes loaded from " + fqcn);
            }
        } else {
            // Versión final: escaneo del paquete base
            List<Class<?>> controllers = ClassScanner.findControllers(DEFAULT_BASE_PACKAGE);
            for (Class<?> c : controllers) {
                Object instance = newControllerInstance(c.getName());
                ensureRestController(c);
                registry.register(instance);
                System.out.println("[ioc] GET routes loaded (scan) " + c.getName());
            }
            if (controllers.isEmpty()) {
                System.out.println("[ioc] No @RestController found under " + DEFAULT_BASE_PACKAGE);
            }
        }

        // 2) Arrancar servidor después
        HttpServer.startServer(new String[]{String.valueOf(DEFAULT_PORT)});
        System.out.println("[http] Listening at http://localhost:" + DEFAULT_PORT + "/");
    }

    private static Object newControllerInstance(String fqcn) throws Exception {
        Class<?> c = Class.forName(fqcn);
        Constructor<?> ctor = c.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
    }

    private static void ensureRestController(Class<?> c) {
        if (!c.isAnnotationPresent(microframework.annotations.RestController.class)) {
            throw new IllegalArgumentException("La clase no está anotada con @RestController: " + c.getName());
        }
    }
}
