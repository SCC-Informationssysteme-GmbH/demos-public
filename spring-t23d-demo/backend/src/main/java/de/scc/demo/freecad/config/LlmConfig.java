package de.scc.demo.freecad.config;

import de.scc.demo.freecad.service.LlmCodeGenerator;
import de.scc.demo.freecad.service.OpenAiCodeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfig {

	@Bean
	public LlmCodeGenerator llmCodeGenerator(AppProperties properties) {
		AppProperties.Llm llm = properties.llm();
		return switch (llm.provider()) {
			case OPENAI -> new OpenAiCodeGenerator(llm.openai());
			case CLAUDE -> throw new IllegalStateException("LLM-Provider CLAUDE ist aktuell nicht implementiert");
		};
	}
}
