package com.ordermanagement.customer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Management System API")
                        .version("1.0")
                        .description("""
                            This API manages customers and their addresses.

                            Business Rules:
                            - Email must be unique
                            - Phone must be unique
                            - At least one address is required
                            - Exactly one address must be default
                            - Only one default address per customer
                            """)
                );
    }
}