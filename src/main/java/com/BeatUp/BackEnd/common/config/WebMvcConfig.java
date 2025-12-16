package com.BeatUp.BackEnd.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스 핸들러는 특정 경로에만 적용
        // API 경로(/concerts, /concert 등)는 제외하여 컨트롤러가 처리하도록 함
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}

