package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.config.WebConfig;

import org.springframework.lang.NonNull;
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
    @NonNull
    protected String[] getServletMappings() {
        return new String[]{"/"}; 
    }
}
