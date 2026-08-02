package de.scc.demo.t23d.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI textTo3dOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Text-to-3D API")
				.description("Generiert FreeCAD-Modelle aus Text-Prompts per LLM")
				.version("v1"));
	}
}
