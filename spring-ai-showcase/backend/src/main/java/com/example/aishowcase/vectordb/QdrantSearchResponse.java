package com.example.aishowcase.vectordb;

import java.util.List;
import java.util.Map;

record QdrantSearchResponse(List<Result> result) {
    record Result(String id, double score, Map<String, Object> payload) {
    }
}
