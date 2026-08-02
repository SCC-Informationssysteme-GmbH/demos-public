package de.scc.demo.t23d.exception;

import de.scc.demo.t23d.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.reduce((a, b) -> a + "; " + b)
				.orElse("Ungueltige Anfrage");
		return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
	}

	@ExceptionHandler(GenerationException.class)
	public ResponseEntity<ErrorResponse> handleGenerationException(GenerationException ex) {
		log.warn("Generierung fehlgeschlagen ({}): {}", ex.getType(), ex.getMessage());
		HttpStatus status = switch (ex.getType()) {
			case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
			case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
			case LLM_ERROR, FREECAD_ERROR, OUTPUT_ERROR -> HttpStatus.BAD_GATEWAY;
		};
		return ResponseEntity.status(status)
				.body(new ErrorResponse(ex.getType().name(), ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		log.error("Unerwarteter Fehler", ex);
		return ResponseEntity.internalServerError()
				.body(new ErrorResponse("INTERNAL_ERROR", "Unerwarteter Fehler bei der Modell-Generierung"));
	}
}
