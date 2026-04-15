package com.kuopan.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AppConfig appConfig;
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射静态资源
        registry.addResourceHandler("/file_res/**")
                .addResourceLocations("file:" + appConfig.getProjectFolder());
    }
}