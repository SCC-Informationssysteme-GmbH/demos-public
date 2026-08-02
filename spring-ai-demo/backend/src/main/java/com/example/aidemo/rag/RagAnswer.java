package com.example.aidemo.rag;

import java.util.List;

public record RagAnswer(List<RagDocument> sources, String answer) {
}
