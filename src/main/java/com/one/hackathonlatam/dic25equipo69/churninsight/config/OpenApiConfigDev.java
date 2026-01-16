package com.one.hackathonlatam.dic25equipo69.churninsight.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile({"dev", "test"})
public class OpenApiConfigDev {

    @org.springframework.context.annotation.Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ChurnInsight API - Development")
                        .version("1.0.0")
                        .description("""
                            **Endpoints disponibles:**
                    * `POST /api/v1/predict` - Predicción de churn
                    * `GET /api/v1/swagger-ui.html` - Documentación interactiva
                    
                    **Mock Service activo**
                    """)
                        .contact(new Contact()
                                .name("Equipo Hackathon ONE Latam")
                                .url("https://github.com/HackathonONELatam-ChurnInsight"))
                        .termsOfService("https://github.com/HackathonONELatam-ChurnInsight/blob/main/LICENSE"))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/v1").description("DEV"),
                        new Server().url("http://localhost:0/api/v1").description("TEST")
                ));
    }
}