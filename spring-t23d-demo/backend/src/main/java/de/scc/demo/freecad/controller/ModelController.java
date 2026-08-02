package de.scc.demo.freecad.controller;

import de.scc.demo.freecad.config.AppProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/models")
public class ModelController {

	private static final Pattern ID_PATTERN = Pattern.compile("^[a-fA-F0-9-]{36}$");
	private static final MediaType STL_MEDIA_TYPE = MediaType.parseMediaType("model/stl");

	private final Path modelsDir;

	public ModelController(AppProperties properties) {
		this.modelsDir = Path.of(properties.freecad().modelsDir()).normalize();
	}

	@GetMapping("/{id}.stl")
	public ResponseEntity<Resource> getModel(@PathVariable String id) {
		if (!ID_PATTERN.matcher(id).matches()) {
			return ResponseEntity.badRequest().build();
		}
		Path file = modelsDir.resolve(id + ".stl").normalize();
		if (!file.startsWith(modelsDir) || !Files.exists(file)) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok()
				.contentType(STL_MEDIA_TYPE)
				.body(new FileSystemResource(file));
	}
}
