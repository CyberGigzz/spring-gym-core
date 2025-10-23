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
        registrationBean.addUrlPatterns("/*"); // Apply to all URLs
        registrationBean.setName("loggingFilter");
        registrationBean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST)); // Only filter incoming requests
        registrationBean.setOrder(1); // Set the order if you have multiple filters

        return registrationBean;
    }
}