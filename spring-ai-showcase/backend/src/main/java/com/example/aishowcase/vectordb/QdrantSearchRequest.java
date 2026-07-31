package com.example.aishowcase.vectordb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record QdrantSearchRequest(List<Double> vector, int limit, @JsonProperty("with_payload") boolean withPayload) {
}
