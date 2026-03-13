package org.example.technicaldocs.model;

public record TechnicalSearchResult(
        TechnicalDocumentChunk chunk,
        double similarityScore
) {
    public TechnicalSearchResult {
        if (chunk == null) {
            throw new IllegalArgumentException("Chunk cannot be null");
        }
    }
}