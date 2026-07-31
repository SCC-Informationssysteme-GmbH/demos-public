package de.scc.demo.tts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TtsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TtsBackendApplication.class, args);
	}

}
