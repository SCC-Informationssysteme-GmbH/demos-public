package de.scc.demo.t23d;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TextTo3dApplication {

	public static void main(String[] args) {
		SpringApplication.run(TextTo3dApplication.class, args);
	}

}
