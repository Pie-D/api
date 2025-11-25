package org.example.gstmeetapi.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200",
                        "http://localhost:4200",
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "https://localhost:8080",
                        "https://sec.cmcati.vn",
                        "https://meet.cmcati.vn/",
                        "https://cmeet.cmcati.vn/",
                        "http://10.1.6.53:4203",
                        "http://10.1.6.53:4204",
                        "https://meet-dev.cmcati.vn",
                        "https://meet.cmcati.vn",
                        "http://10.1.6.11:8085",
                        "https://10.1.6.11:8085")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
