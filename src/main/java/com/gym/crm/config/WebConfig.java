package com.gym.crm.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import; 
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
// import com.gym.crm.config.H2Config;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan("com.gym.crm.controller")
@Import({
    SpringDocConfig.class, 
    AppConfig.class, 
    OpenApiConfig.class,
    // H2Config.class
}) 
public class WebConfig implements WebMvcConfigurer {



}