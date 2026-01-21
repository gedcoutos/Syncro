package com.syncro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${syncro.storage-root:uploads}")
    private String root;

    @Value("${syncro.fotos-dir:fotos}")
    private String fotosDir;

    @Value("${syncro.os-dir:os}")
    private String osDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path fotosPath = Paths.get(root, fotosDir).toAbsolutePath().normalize();
        Path osPath = Paths.get(root, osDir).toAbsolutePath().normalize();

        registry.addResourceHandler("/fotos/**")
                .addResourceLocations(fotosPath.toUri().toString());

        registry.addResourceHandler("/os/**")
                .addResourceLocations(osPath.toUri().toString());

        registry.addResourceHandler("/fotos/**")
                .addResourceLocations("file:" + Paths.get("uploads", "fotos")
                        .toAbsolutePath().normalize().toString() + "/");

        registry.addResourceHandler("/os/**")
                .addResourceLocations("file:" + Paths.get("uploads", "os")
                        .toAbsolutePath().normalize().toString() + "/");

    }
}

