package com.miji.cms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 http://localhost:8080/files/** 映射到本地 D:/AboutCode/SchoolProject/cms/uploads/
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:D:/AboutCode/SchoolProject/cms/uploads/");
    }
}
