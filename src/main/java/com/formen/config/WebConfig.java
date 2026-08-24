package com.formen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/");
        // Compatibilidad con ventas antiguas que guardaron la captura como "pagos/archivo.png".
        registry.addResourceHandler("/pagos/**").addResourceLocations("file:uploads/pagos/");
    }
}
