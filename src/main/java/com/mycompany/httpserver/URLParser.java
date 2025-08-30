/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.httpserver;

import java.net.MalformedURLException;
import java.net.URL;
 
/**
*
* @author sergio.bejarano-r
*/
public class URLParser 
{
    public static void main (String[] args) throws MalformedURLException 
    {
        URL myurl = new URL("http://arep.curso.escuelaing.edu.co/archivos/ejemplo.html#");
        System.out.println("Protocol: "+myurl.getProtocol());
        System.out.println("Authority: "+myurl.getAuthority());
        System.out.println("Host: "+myurl.getHost());
        System.out.println("Port: "+myurl.getPort());
        System.out.println("getPath: "+myurl.getPath());
        System.out.println("getQuery: "+myurl.getQuery());
        System.out.println("getFile: "+myurl.getFile());
        System.out.println("Ref: "+myurl.getRef());
    }
}
