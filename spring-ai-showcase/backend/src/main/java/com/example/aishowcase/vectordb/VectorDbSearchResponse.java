package com.example.aishowcase.vectordb;

import java.util.List;

public record VectorDbSearchResponse(String channel, String module, String query, List<VectorMatch> matches) {
}
