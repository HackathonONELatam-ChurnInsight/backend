package com.one.hackathonlatam.dic25equipo69.churninsight.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("prod")
public class OpenApiConfig {

    @org.springframework.context.annotation.Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        Server prodServer = new Server()
                .url("https://churninsight-api.prod.example.com")
                .description("Production Server");

        return new OpenAPI()
                .info(new Info()
                        .title("ChurnInsight API - Production")
                        .version("1.0.0")
                        .description("API para predicción de churn de clientes - PRODUCCIÓN")
                        .termsOfService("https://churninsight.com/terms")
                        .license(new io.swagger.v3.oas.models.info.License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(prodServer));
    }
}
