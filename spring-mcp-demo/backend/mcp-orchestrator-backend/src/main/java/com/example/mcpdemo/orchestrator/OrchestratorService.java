package com.example.mcpdemo.orchestrator;

import com.example.mcpdemo.orchestrator.agent.AgentResult;
import com.example.mcpdemo.orchestrator.agent.AgentStep;
import com.example.mcpdemo.orchestrator.agent.RecordingToolCallback;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OrchestratorService {

    private static final String SYSTEM_PROMPT = """
            Du beantwortest Fragen zu einer Buchhandlung- und Kundenverwaltung-Demo.
            Die Oberfläche stellt deine Antwort als reinen Text dar (kein Markdown-,
            kein LaTeX-Renderer). Antworte deshalb in normalem Fließtext ohne
            LaTeX-Notation (kein \\( \\), kein \\,, keine anderen Escape-Sequenzen) und
            ohne Markdown-Formatierungszeichen wie ** für Fettschrift.

            Wenn nach einer Summe, Gesamtausgaben oder einem Gesamtbetrag gefragt wird
            (z. B. "Gesamtsumme", "wie viel ausgegeben"), nutze IMMER das Tool
            get_total_spent für die Berechnung. Rechne Summen niemals selbst aus
            Einzelpreisen zusammen - das Tool liefert den serverseitig berechneten,
            verlässlichen Wert.
            """;

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;

    public OrchestratorService(ChatClient chatClient, ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = chatClient;
        this.toolCallbackProvider = toolCallbackProvider;
    }

    public String chat(String message) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .toolCallbacks(toolCallbackProvider)
                .call()
                .content();
    }

    public AgentResult runAgent(String task) {
        List<AgentStep> steps = new ArrayList<>();
        List<ToolCallback> recordedTools = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(tool -> (ToolCallback) new RecordingToolCallback(tool, steps))
                .toList();

        String answer = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(task)
                .toolCallbacks(recordedTools)
                .call()
                .content();

        return new AgentResult(answer, steps);
    }
}
