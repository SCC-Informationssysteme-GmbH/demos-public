package de.scc.demo.freecad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PromptTo3dApplication {

	public static void main(String[] args) {
		SpringApplication.run(PromptTo3dApplication.class, args);
	}

}
