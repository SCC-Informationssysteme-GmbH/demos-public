package com.example.aishowcase.vectordb;

record QdrantCreateCollectionRequest(VectorsConfig vectors) {
    record VectorsConfig(int size, String distance) {
    }
}
