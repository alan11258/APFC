package com.tpt.apfc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")          // 允許所有的 API 路徑
                        .allowedOrigins("http://localhost:5173", "https://your-production-domain.com") // 允許的來源域名
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允許的 HTTP 方法
                        .allowedHeaders("*")        // 允許所有的 Header
                        .allowCredentials(true)    // 是否允許傳送 Cookie (重要！)
                        .maxAge(3600);             // 預檢請求 (Preflight) 的快取時間（秒）
            }
        };
    }

}
