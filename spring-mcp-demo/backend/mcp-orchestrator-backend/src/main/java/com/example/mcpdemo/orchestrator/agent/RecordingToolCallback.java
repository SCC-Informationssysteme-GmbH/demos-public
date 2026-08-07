package com.example.mcpdemo.orchestrator.agent;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import java.util.List;

/**
 * Wraps an MCP-Tool-Callback und protokolliert jeden Aufruf, damit der Agenten-Modus
 * die Zwischenschritte einer mehrschrittigen Aufgabe sichtbar machen kann.
 */
public class RecordingToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final List<AgentStep> steps;

    public RecordingToolCallback(ToolCallback delegate, List<AgentStep> steps) {
        this.delegate = delegate;
        this.steps = steps;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        String output = delegate.call(toolInput);
        steps.add(new AgentStep(delegate.getToolDefinition().name(), toolInput, output));
        return output;
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        String output = delegate.call(toolInput, toolContext);
        steps.add(new AgentStep(delegate.getToolDefinition().name(), toolInput, output));
        return output;
    }
}
