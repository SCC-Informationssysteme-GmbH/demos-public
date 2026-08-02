package com.example.aidemo.rag;

import java.util.List;

public record RagAskResponse(String channel, String module, String question, List<RagSourceSnippet> sources, String answer) {
}
