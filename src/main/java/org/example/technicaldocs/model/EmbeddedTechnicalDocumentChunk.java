package org.example.technicaldocs.model;

import java.util.List;

public record EmbeddedTechnicalDocumentChunk(
        TechnicalDocumentChunk chunk,
        List<Double> embedding
) {
    public EmbeddedTechnicalDocumentChunk {
        if (chunk == null) {
            throw new IllegalArgumentException("Chunk cannot be null");
        }

        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException("Embedding cannot be null or empty");
        }
    }
}