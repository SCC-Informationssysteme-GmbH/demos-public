package com.example.aishowcase.rag;

import java.util.List;

record OpenAiEmbeddingResponse(List<Data> data) {
    record Data(List<Double> embedding) {
    }
}
