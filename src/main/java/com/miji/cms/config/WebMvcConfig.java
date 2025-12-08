package com.miji.cms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 http://localhost:8080/uploads/** 映射到配置的上传路径
        String absolutePath = new File(uploadPath).getAbsolutePath() + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
