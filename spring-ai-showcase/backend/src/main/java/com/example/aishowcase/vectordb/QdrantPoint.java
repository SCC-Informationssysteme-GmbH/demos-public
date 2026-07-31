package com.example.aishowcase.vectordb;

import java.util.List;
import java.util.Map;

record QdrantPoint(String id, List<Double> vector, Map<String, Object> payload) {
}
