package com.demo.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Displays the Authorize option in the top-right corner of the swagger ui, allowing users to input their JWT token for authentication when testing the API endpoints. 
 * This configuration is essential for securing the API documentation and ensuring that only authorized users can access protected endpoints through the Swagger UI.
 * By defining a security scheme for Bearer Authentication, we enable the Swagger UI to recognize and utilize JWT tokens for API testing, enhancing the security and usability of the API documentation.        
 * 
 */
@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}