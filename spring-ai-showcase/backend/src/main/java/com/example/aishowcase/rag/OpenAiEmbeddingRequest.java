package com.example.aishowcase.rag;

import java.util.List;

record OpenAiEmbeddingRequest(String model, List<String> input) {
}
