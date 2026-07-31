package com.example.aishowcase.rag;

import java.util.List;

public record RagAnswer(List<RagDocument> sources, String answer) {
}
