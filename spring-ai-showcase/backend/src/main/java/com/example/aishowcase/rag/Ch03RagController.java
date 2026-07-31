package com.example.aishowcase.rag;

import com.example.aishowcase.common.ChannelStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ch03")
public class Ch03RagController {

    private final RagKnowledgeBase knowledgeBase;
    private final RagService ragService;

    public Ch03RagController(RagKnowledgeBase knowledgeBase, RagService ragService) {
        this.knowledgeBase = knowledgeBase;
        this.ragService = ragService;
    }

    @GetMapping("/status")
    public ChannelStatusResponse status() {
        return new ChannelStatusResponse("CH.03", "rag", "RAG bereit");
    }

    @GetMapping("/documents")
    public List<RagDocumentSummary> documents() {
        return knowledgeBase.all().stream()
                .map(document -> new RagDocumentSummary(document.id(), document.title()))
                .toList();
    }

    @PostMapping("/ask")
    public RagAskResponse ask(@RequestBody RagAskRequest request) {
        RagAnswer answer = ragService.ask(request.question());
        List<RagSourceSnippet> sources = answer.sources().stream()
                .map(document -> new RagSourceSnippet(document.id(), document.title()))
                .toList();
        return new RagAskResponse("CH.03", "rag", request.question(), sources, answer.answer());
    }
}
