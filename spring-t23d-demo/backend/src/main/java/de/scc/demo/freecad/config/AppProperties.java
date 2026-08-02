package de.scc.demo.freecad.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Llm llm, Freecad freecad) {

	public record Llm(Provider provider, OpenAi openai) {

		public enum Provider {
			OPENAI, CLAUDE
		}

		public record OpenAi(String apiKey, String model, String baseUrl) {
		}
	}

	public record Freecad(
			String dockerImage,
			int timeoutSeconds,
			String memoryLimit,
			String cpuLimit,
			String scriptsDir,
			String scriptsDirHost,
			String modelsDir) {

		/**
		 * Host-Pfad des Skript-Ordners, wie ihn der Docker-Daemon fuer den
		 * "docker run -v"-Aufruf sehen muss. Laeuft das Backend selbst nicht
		 * containerisiert, ist das derselbe Pfad wie {@link #scriptsDir()}.
		 * Laeuft das Backend containerisiert (docker-compose), muss
		 * scriptsDirHost auf den tatsaechlichen Host-Pfad des gemounteten
		 * Verzeichnisses gesetzt werden, da der Docker-Daemon Bind-Mounts
		 * immer relativ zum Host, nicht zum aufrufenden Container, aufloest.
		 */
		public String resolvedScriptsDirHost() {
			return (scriptsDirHost == null || scriptsDirHost.isBlank()) ? scriptsDir : scriptsDirHost;
		}
	}
}
