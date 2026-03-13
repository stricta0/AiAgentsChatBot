package org.example.technicaldocs.model;

public record TechnicalDocumentChunk(
        String documentName,
        String documentType,
        String headingPath,
        int chunkIndex,
        String content
) {
}