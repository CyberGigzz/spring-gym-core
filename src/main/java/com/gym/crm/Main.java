package com.gym.crm;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;


import java.io.File;

public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.getConnector();

        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        Context context = tomcat.addWebapp("", baseDir.getAbsolutePath());

        System.out.println("Starting Tomcat server...");
        tomcat.start();

        System.out.println("====================================================================");
        System.out.println("  Tomcat server started on port: " + PORT);
        System.out.println("  Application URL: http://localhost:" + PORT);
        System.out.println("  Swagger UI available at: http://localhost:" + PORT + "/swagger-ui.html");
        System.out.println("  H2 Console available at: http://localhost:" + PORT + "/h2-console/");
        System.out.println("====================================================================");

        tomcat.getServer().await();
    }
}