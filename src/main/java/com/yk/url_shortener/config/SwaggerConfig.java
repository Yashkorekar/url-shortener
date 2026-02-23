package com.yk.url_shortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.base.url:http://localhost:8081}")
    private String baseUrl;

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .description("A REST API service that shortens long URLs into compact, shareable links. " +
                                "Built with Spring Boot, this service provides URL shortening, redirection, " +
                                "statistics tracking, and domain metrics.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Yash Korekar")
                                .email("yashkorekar23@gmail.com")
                                .url("https://github.com/Yashkorekar"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url(baseUrl)
                                .description("Development Server"),
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local Server")
                ));
    }
}

