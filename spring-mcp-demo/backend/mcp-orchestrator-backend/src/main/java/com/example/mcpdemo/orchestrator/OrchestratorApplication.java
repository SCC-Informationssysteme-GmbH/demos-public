package com.example.mcpdemo.orchestrator;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "MCP Orchestrator",
        description = "Haelt den OpenAI-Key, ist MCP-Client fuer die Buchhandlung- und "
                + "Kundenverwaltung-mcp-server und bietet Chat- (Tool-Calling) und "
                + "Agenten-Endpunkte (mehrschrittig, mit sichtbaren Zwischenschritten) "
                + "fuers Frontend.",
        version = "0.1.0"
))
public class OrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApplication.class, args);
    }
}
