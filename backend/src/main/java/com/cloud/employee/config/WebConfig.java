package com.cloud.employee.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.mapping}")
    private String mapping;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /api/v1/files/** to local filesystem path
        // file: prefix is mandatory for absolute windows paths
        String location = "file:" + uploadPath;
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler(mapping)
                .addResourceLocations(location);
    }
}
