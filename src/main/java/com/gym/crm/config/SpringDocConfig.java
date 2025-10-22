package com.gym.crm.config;

// Note: This 'boot' import is correct. The 'springdoc' library
// downloads this for you as a dependency.
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;

@Configuration
@ComponentScan(basePackages = "org.springdoc") // Scan the springdoc library for its beans
@Import({
    // Manually import all the configuration classes springdoc needs
    SpringDocConfiguration.class,
    SpringDocWebMvcConfiguration.class,
    org.springdoc.webmvc.ui.SwaggerConfig.class,
    SpringDocConfigProperties.class,
    SwaggerUiConfigProperties.class,
    SwaggerUiOAuthProperties.class,
    JacksonAutoConfiguration.class 
})
public class SpringDocConfig {
    // This class is just for configuration imports
}