package de.scc.demo.freecad.service;

import de.scc.demo.freecad.config.AppProperties;
import de.scc.demo.freecad.exception.GenerationException;
import de.scc.demo.freecad.exception.GenerationException.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Fuehrt LLM-generierten FreeCAD-Python-Code isoliert in einem Docker-Container
 * aus (Anforderungen 4.1/4.4/4.5) und legt die erzeugte STL-Datei dauerhaft im
 * Modell-Verzeichnis ab.
 */
@Service
public class FreecadExecutionService {

	private static final Logger log = LoggerFactory.getLogger(FreecadExecutionService.class);
	private static final String SCRIPT_FILENAME = "model.py";
	private static final String OUTPUT_FILENAME = "output.stl";

	private final AppProperties.Freecad config;

	public FreecadExecutionService(AppProperties properties) {
		this.config = properties.freecad();
	}

	/**
	 * @return die generierte Modell-ID, unter der die STL-Datei abrufbar ist
	 */
	public String execute(String freecadCode) {
		String id = UUID.randomUUID().toString();
		Path scriptDir = Path.of(config.scriptsDir(), id);
		Path scriptFile = scriptDir.resolve(SCRIPT_FILENAME);

		try {
			Files.createDirectories(scriptDir);
			Files.writeString(scriptFile, freecadCode);
		} catch (IOException ex) {
			throw new GenerationException(ErrorType.FREECAD_ERROR, "Skript konnte nicht geschrieben werden", ex);
		}

		String containerLog = runDockerContainer(scriptDir, id);

		Path outputStl = scriptDir.resolve(OUTPUT_FILENAME);
		if (!Files.exists(outputStl) || isEmpty(outputStl)) {
			// freecadcmd beendet sich bei einer Skript-Exception haeufig trotzdem mit Exit-Code 0,
			// daher ist die Ausgabe des Containers hier die einzige verlaessliche Fehlerquelle.
			throw new GenerationException(ErrorType.OUTPUT_ERROR,
					"FreeCAD hat keine gueltige STL-Datei erzeugt. FreeCAD-Ausgabe: " + containerLog);
		}

		return storeModel(id, outputStl);
	}

	private String runDockerContainer(Path scriptDir, String id) {
		String hostScriptDir = Path.of(config.resolvedScriptsDirHost(), id).toString();
		ProcessBuilder builder = new ProcessBuilder(
				"docker", "run", "--rm",
				"--network", "none",
				"--memory", config.memoryLimit(),
				"--cpus", config.cpuLimit(),
				"-v", hostScriptDir + ":/work",
				config.dockerImage(),
				"/work/" + SCRIPT_FILENAME);
		builder.redirectErrorStream(true);

		Process process;
		try {
			process = builder.start();
		} catch (IOException ex) {
			throw new GenerationException(ErrorType.FREECAD_ERROR, "FreeCAD-Container konnte nicht gestartet werden", ex);
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Thread drainThread = drainAsync(process.getInputStream(), output);

		boolean finishedInTime;
		try {
			finishedInTime = process.waitFor(config.timeoutSeconds(), TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			process.destroyForcibly();
			throw new GenerationException(ErrorType.FREECAD_ERROR, "Ausfuehrung wurde unterbrochen", ex);
		}

		if (!finishedInTime) {
			process.destroyForcibly();
			throw new GenerationException(ErrorType.TIMEOUT,
					"FreeCAD-Ausfuehrung hat das Zeitlimit von " + config.timeoutSeconds() + "s ueberschritten");
		}

		joinQuietly(drainThread);
		String containerLog = output.toString();
		log.debug("FreeCAD-Ausgabe: {}", containerLog);

		if (process.exitValue() != 0) {
			log.warn("FreeCAD-Ausfuehrung fehlgeschlagen: {}", containerLog);
			throw new GenerationException(ErrorType.FREECAD_ERROR, "FreeCAD-Skript ist fehlgeschlagen: " + containerLog);
		}

		return containerLog;
	}

	private String storeModel(String id, Path outputStl) {
		try {
			Path modelsDir = Path.of(config.modelsDir());
			Files.createDirectories(modelsDir);
			Path target = modelsDir.resolve(id + ".stl");
			Files.copy(outputStl, target, StandardCopyOption.REPLACE_EXISTING);
			return id;
		} catch (IOException ex) {
			throw new GenerationException(ErrorType.OUTPUT_ERROR, "STL-Datei konnte nicht abgelegt werden", ex);
		}
	}

	private boolean isEmpty(Path file) {
		try {
			return Files.size(file) == 0;
		} catch (IOException ex) {
			return true;
		}
	}

	private Thread drainAsync(InputStream in, ByteArrayOutputStream out) {
		Thread thread = new Thread(() -> {
			try {
				in.transferTo(out);
			} catch (IOException ex) {
				log.debug("Prozess-Ausgabe konnte nicht vollstaendig gelesen werden", ex);
			}
		}, "freecad-output-drain");
		thread.setDaemon(true);
		thread.start();
		return thread;
	}

	private void joinQuietly(Thread thread) {
		try {
			thread.join(2000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}
