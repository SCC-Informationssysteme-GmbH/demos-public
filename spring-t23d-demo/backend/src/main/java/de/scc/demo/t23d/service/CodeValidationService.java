package de.scc.demo.t23d.service;

import de.scc.demo.t23d.exception.GenerationException;
import de.scc.demo.t23d.exception.GenerationException.ErrorType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Einfache statische Pruefung des LLM-generierten Codes auf verdaechtige
 * Muster, bevor er im FreeCAD-Container ausgefuehrt wird (Anforderung 4.4).
 * Ersetzt NICHT die Container-Isolation (--network none, keine zusaetzlichen
 * Mounts, Ressourcenlimits), sondern ergaenzt sie.
 */
@Service
public class CodeValidationService {

	private static final List<Pattern> FORBIDDEN_PATTERNS = List.of(
			Pattern.compile("\\bimport\\s+os\\b"),
			Pattern.compile("\\bimport\\s+sys\\b"),
			Pattern.compile("\\bimport\\s+shutil\\b"),
			Pattern.compile("\\bimport\\s+subprocess\\b"),
			Pattern.compile("\\bsubprocess\\b"),
			Pattern.compile("\\bsocket\\b"),
			Pattern.compile("\\bopen\\s*\\("),
			Pattern.compile("\\b__import__\\s*\\("),
			Pattern.compile("\\bimportlib\\b"),
			Pattern.compile("\\beval\\s*\\("),
			Pattern.compile("\\bexec\\s*\\("),
			Pattern.compile("\\bctypes\\b"),
			Pattern.compile("\\bimport\\s+urllib\\b"),
			Pattern.compile("\\bimport\\s+requests\\b"));

	public void validate(String code) {
		for (Pattern pattern : FORBIDDEN_PATTERNS) {
			if (pattern.matcher(code).find()) {
				throw new GenerationException(ErrorType.VALIDATION_ERROR,
						"Generierter Code enthaelt ein verbotenes Muster: " + pattern.pattern());
			}
		}
	}
}
