package com.orderflow.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderFlowOpenAPI() {

        return new OpenAPI()

                .info(new Info()

                        .title("OrderFlow API")

                        .description("Production Ready Order Management REST API")

                        .version("v1.0")

                        .contact(new Contact()

                                .name("Praveen Bandi")

                                .email("praveen@example.com"))

                        .license(new License()

                                .name("Apache 2.0")))

                .externalDocs(

                        new ExternalDocumentation()

                                .description("Project Documentation")

                                .url("https://github.com/")
                );
    }

}