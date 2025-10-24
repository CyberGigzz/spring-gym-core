package com.gym.crm.config;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilterRegistration() {
        FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new LoggingFilter());
        registrationBean.addUrlPatterns("/*"); 
        registrationBean.setName("loggingFilter");
        registrationBean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST)); 
        registrationBean.setOrder(1); 

        return registrationBean;
    }
}