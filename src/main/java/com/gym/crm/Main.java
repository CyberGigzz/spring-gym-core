package com.gym.crm;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import java.io.File;
import org.h2.server.web.JakartaWebServlet;


public class Main {

    private static final int PORT = 8080;
    

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.getConnector(); 

        String contextPath = "/";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        AnnotationConfigWebApplicationContext springContext = new AnnotationConfigWebApplicationContext();
        springContext.register(com.gym.crm.config.WebConfig.class);
        DispatcherServlet dispatcherServlet = new DispatcherServlet(springContext);

        String servletName = "dispatcher";
        Tomcat.addServlet(context, servletName, dispatcherServlet);
        context.addServletMappingDecoded("/", servletName);

        String h2ServletName = "H2Console";
        JakartaWebServlet h2Servlet = new JakartaWebServlet();
        Tomcat.addServlet(context, h2ServletName, h2Servlet);
        context.addServletMappingDecoded("/h2-console/*", h2ServletName);

        System.out.println("Starting Tomcat server with Spring on port: " + PORT);
        tomcat.start();
        tomcat.getServer().await();
    }
}