package com.example.aiagents.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;

/**
 * Speichert die Agenten-Ergebnisse (Records) als JSON-Spalte.
 * So bleibt jedes Zwischenergebnis einzeln nachvollziehbar, ohne dass die
 * DTOs zu JPA-Embeddables umgebaut werden muessen.
 */
abstract class JsonAttributeConverter<T> implements AttributeConverter<T, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Class<T> type;

    protected JsonAttributeConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Serialisierung fehlgeschlagen: " + type.getSimpleName(), e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(dbData, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Deserialisierung fehlgeschlagen: " + type.getSimpleName(), e);
        }
    }
}
