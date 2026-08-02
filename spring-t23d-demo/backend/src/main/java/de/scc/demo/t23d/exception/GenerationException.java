package de.scc.demo.t23d.exception;

public class GenerationException extends RuntimeException {

	public enum ErrorType {
		LLM_ERROR, VALIDATION_ERROR, TIMEOUT, FREECAD_ERROR, OUTPUT_ERROR
	}

	private final ErrorType type;

	public GenerationException(ErrorType type, String message) {
		super(message);
		this.type = type;
	}

	public GenerationException(ErrorType type, String message, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	public ErrorType getType() {
		return type;
	}
}
