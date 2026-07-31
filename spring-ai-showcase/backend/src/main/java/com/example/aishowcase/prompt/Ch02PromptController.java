package com.example.aishowcase.prompt;

import com.example.aishowcase.common.ChannelStatusResponse;
import com.example.aishowcase.llmrest.OpenAiChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ch02")
public class Ch02PromptController {

    private final PromptTemplateService templateService;
    private final OpenAiChatService chatService;

    public Ch02PromptController(PromptTemplateService templateService, OpenAiChatService chatService) {
        this.templateService = templateService;
        this.chatService = chatService;
    }

    @GetMapping("/status")
    public ChannelStatusResponse status() {
        return new ChannelStatusResponse("CH.02", "prompt", "Prompt-Orchestrierung bereit");
    }

    @GetMapping("/templates")
    public List<PromptTemplateSummary> templates() {
        return templateService.all().stream()
                .map(template -> new PromptTemplateSummary(template.key(), template.label()))
                .toList();
    }

    @PostMapping("/chat")
    public PromptChatResponse chat(@RequestBody PromptChatRequest request) {
        PromptTemplate template = templateService.get(request.templateKey());
        String renderedPrompt = template.render(request.input());
        String reply = chatService.complete(template.systemPrompt(), renderedPrompt);
        return new PromptChatResponse("CH.02", "prompt", template.key(), renderedPrompt, reply);
    }
}
