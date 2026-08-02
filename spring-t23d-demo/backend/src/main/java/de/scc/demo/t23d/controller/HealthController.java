package de.scc.demo.t23d.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class HealthController {

	private static final Logger log = LoggerFactory.getLogger(HealthController.class);

	@GetMapping("/health")
	public ResponseEntity<Map<String, Object>> health() {
		boolean dockerAvailable = isDockerAvailable();
		Map<String, Object> body = Map.of("docker", dockerAvailable);
		return dockerAvailable ? ResponseEntity.ok(body) : ResponseEntity.status(503).body(body);
	}

	private boolean isDockerAvailable() {
		try {
			Process process = new ProcessBuilder("docker", "info").start();
			boolean finished = process.waitFor(5, TimeUnit.SECONDS);
			return finished && process.exitValue() == 0;
		} catch (Exception ex) {
			log.warn("Docker-Health-Check fehlgeschlagen", ex);
			return false;
		}
	}
}
