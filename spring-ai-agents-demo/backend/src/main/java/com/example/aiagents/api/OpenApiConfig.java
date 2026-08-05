package com.example.aiagents.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("Multi-Agenten-Support API")
                .version("0.0.1")
                .description("""
                        Klassifizierung -> Recherche -> Antwortentwurf, freigegeben durch einen Mitarbeiter.

                        Ablauf zum Testen:
                        1. POST /api/tickets - Anfrage anlegen, Workflow laeuft asynchron
                        2. GET /api/tickets/{id} - Zustand und Zwischenergebnisse pollen
                        3. POST /api/tickets/{id}/approve - Entwurf freigeben oder ablehnen

                        Der SSE-Endpoint /api/tickets/{id}/stream laesst sich in der Swagger UI
                        nicht sinnvoll ausprobieren - dafuer das Frontend oder curl nutzen.
                        """));
    }
}
