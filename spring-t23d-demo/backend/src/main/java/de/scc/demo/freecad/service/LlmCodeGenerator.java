package de.scc.demo.freecad.service;

public interface LlmCodeGenerator {

	/**
	 * Erzeugt aus einem Nutzer-Prompt reinen FreeCAD-Python-Code.
	 */
	String generateFreecadScript(String prompt);
}
