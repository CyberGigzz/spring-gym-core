package com.gym.crm;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import java.io.File;

public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.getConnector(); // Initializes the connector

        // 1. Define the application context for Tomcat
        String contextPath = "/";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        // 2. Create the Spring Application Context
        AnnotationConfigWebApplicationContext springContext = new AnnotationConfigWebApplicationContext();
        // Register your Spring configuration class
        springContext.register(com.gym.crm.config.WebConfig.class);

        // 3. Create the Spring DispatcherServlet
        DispatcherServlet dispatcherServlet = new DispatcherServlet(springContext);

        // 4. Register the servlet with Tomcat
        String servletName = "dispatcher";
        Tomcat.addServlet(context, servletName, dispatcherServlet);
        
        // 5. Add the servlet mapping to handle all requests
        context.addServletMappingDecoded("/", servletName);

        System.out.println("Starting Tomcat server with Spring on port: " + PORT);
        tomcat.start();
        tomcat.getServer().await();
    }
}