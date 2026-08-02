package de.scc.demo.freecad.controller;

import de.scc.demo.freecad.dto.GenerateRequest;
import de.scc.demo.freecad.dto.GenerateResponse;
import de.scc.demo.freecad.service.CodeValidationService;
import de.scc.demo.freecad.service.FreecadExecutionService;
import de.scc.demo.freecad.service.LlmCodeGenerator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GenerateController {

	private final LlmCodeGenerator llmCodeGenerator;
	private final CodeValidationService codeValidationService;
	private final FreecadExecutionService freecadExecutionService;

	public GenerateController(LlmCodeGenerator llmCodeGenerator,
			CodeValidationService codeValidationService,
			FreecadExecutionService freecadExecutionService) {
		this.llmCodeGenerator = llmCodeGenerator;
		this.codeValidationService = codeValidationService;
		this.freecadExecutionService = freecadExecutionService;
	}

	@PostMapping("/generate")
	public GenerateResponse generate(@Valid @RequestBody GenerateRequest request) {
		String code = llmCodeGenerator.generateFreecadScript(request.prompt());
		codeValidationService.validate(code);
		String id = freecadExecutionService.execute(code);
		return new GenerateResponse(id, "/api/models/" + id + ".stl");
	}
}
