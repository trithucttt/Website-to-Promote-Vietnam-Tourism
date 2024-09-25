package com.trithuc.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer{
	@Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Đặt danh sách các nguồn được phép truy cập, hoặc "*" để cho phép từ mọi nguồn
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // Đặt danh sách các phương thức HTTP được phép
       // config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.addAllowedMethod("*");

        // Đặt danh sách các header được phép
       // config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.addAllowedHeader("*");

        // Cho phép sử dụng các cookie
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
	
	 @Override
	    public void addCorsMappings(CorsRegistry registry) {
	        registry.addMapping("/api/**")
	                .allowedOrigins("http://localhost:3000") // Add the origin of your React app
	                .allowedMethods("GET", "POST", "PUT", "DELETE")
//                    .allowedOriginPatterns("*")
	                .allowedHeaders("*");
//	                .allowCredentials(true);
	    }
	
}
