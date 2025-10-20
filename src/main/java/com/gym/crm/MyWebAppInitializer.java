package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.config.WebConfig;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{AppConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"}; 
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        ServletRegistration.Dynamic h2ConsoleServlet = servletContext.addServlet("H2Console", "org.h2.server.web.WebServlet");
        h2ConsoleServlet.addMapping("/h2-console/*");
        h2ConsoleServlet.setLoadOnStartup(1); 
    }
}