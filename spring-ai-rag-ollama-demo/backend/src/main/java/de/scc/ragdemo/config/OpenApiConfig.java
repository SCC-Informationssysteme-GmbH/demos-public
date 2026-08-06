package de.scc.ragdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ragDemoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring AI RAG Ollama Demo")
                        .description("RAG-Demo mit Spring AI, Ollama und Qdrant")
                        .version("0.1.0"));
    }
}
