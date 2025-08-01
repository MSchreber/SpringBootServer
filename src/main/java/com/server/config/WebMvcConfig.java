package com.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${postkasten.folder}") //Ordner, in dem die Briefe gespeichert sind
    private String folder;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/wasistinderpost/images/**")
                .addResourceLocations("file:" + folder)
                .resourceChain(false);
    }
}