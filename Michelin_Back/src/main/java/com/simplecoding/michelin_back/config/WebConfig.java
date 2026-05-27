package com.simplecoding.michelin_back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${image.upload-dir:C:/images/}")
    private String uploadDir;

    // ✅ CORS 제거 - SecurityConfig 단일 관리
    // addCorsMappings() 삭제

    // ✅ 이미지 정적 리소스 매핑 통일
    // /images/** → file:///C:/images/ (uploadDir 변수 사용)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/restaurants/**")
                .addResourceLocations("file:C:/Work/restaurants/");
    }

}