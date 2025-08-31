package com.mycompany.microframework.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClassScanner {

    /** Escanea el classpath por clases bajo basePackage con @RestController */
    public static List<Class<?>> findControllers(String basePackage) {
        List<Class<?>> out = new ArrayList<>();
        String path = basePackage.replace('.', '/');
        try {
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                File dir = new File(url.toURI());
                scanDir(basePackage, dir, out);
            }
        } catch (Exception ignored) {}
        return out;
    }

    private static void scanDir(String basePackage, File dir, List<Class<?>> out) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDir(basePackage + "." + f.getName(), f, out);
            } else if (f.getName().endsWith(".class")) {
                String clsName = basePackage + "." + f.getName().substring(0, f.getName().length() - 6);
                try {
                    Class<?> c = Class.forName(clsName);
                    if (c.isAnnotationPresent(microframework.annotations.RestController.class)) {
                        out.add(c);
                    }
                } catch (Throwable ignored) {}
            }
        }
    }
}
