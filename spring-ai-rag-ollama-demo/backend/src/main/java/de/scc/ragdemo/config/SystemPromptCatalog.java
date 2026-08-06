package de.scc.ragdemo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Laedt die auswaehlbaren System-Prompts (Alias, Anzeigename, Prompt-Text)
 * aus {@code system-prompts.json}, damit sie ohne Code-Aenderung angepasst
 * werden koennen und im Frontend als Dropdown angeboten werden.
 */
@Component
public class SystemPromptCatalog {

    public record SystemPromptOption(String alias, String label, String systemPrompt, boolean requiresContext) {}

    private record CatalogData(String defaultAlias, List<SystemPromptOption> options) {}

    private final List<SystemPromptOption> options;
    private final Map<String, SystemPromptOption> byAlias;
    private final String defaultAlias;

    public SystemPromptCatalog() {
        CatalogData data;
        try (var in = new ClassPathResource("system-prompts.json").getInputStream()) {
            data = new ObjectMapper().readValue(in, CatalogData.class);
        }
        catch (IOException e) {
            throw new UncheckedIOException("system-prompts.json konnte nicht geladen werden", e);
        }
        this.options = data.options();
        this.defaultAlias = data.defaultAlias();
        this.byAlias = options.stream().collect(Collectors.toMap(SystemPromptOption::alias, Function.identity()));
    }

    public List<SystemPromptOption> options() {
        return options;
    }

    public String defaultPrompt() {
        return promptForAlias(defaultAlias);
    }

    public String promptForAlias(String alias) {
        return resolve(alias).systemPrompt();
    }

    /**
     * Ob diese Persona nur auf Basis von echt gefundenem Kontext antworten soll
     * (siehe {@link de.scc.ragdemo.controller.ChatController}, das dafuer selbst
     * eine Aehnlichkeitssuche macht statt sich auf den Prompt zu verlassen).
     */
    public boolean requiresContext(String alias) {
        return resolve(alias).requiresContext();
    }

    private SystemPromptOption resolve(String alias) {
        SystemPromptOption option = StringUtils.hasText(alias) ? byAlias.get(alias) : null;
        if (option == null) {
            option = byAlias.getOrDefault(defaultAlias, options.get(0));
        }
        return option;
    }
}
