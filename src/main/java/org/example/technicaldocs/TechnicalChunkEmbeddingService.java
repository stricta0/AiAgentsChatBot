package org.example.technicaldocs;

import org.example.technicaldocs.model.EmbeddedTechnicalDocumentChunk;
import org.example.technicaldocs.model.TechnicalDocument;
import org.example.technicaldocs.model.TechnicalDocumentChunk;

import java.util.ArrayList;
import java.util.List;

public class TechnicalChunkEmbeddingService {

    private final TechnicalDocumentChunker chunker;
    private final EmbeddingClient embeddingClient;

    public TechnicalChunkEmbeddingService(
            TechnicalDocumentChunker chunker,
            EmbeddingClient embeddingClient
    ) {
        this.chunker = chunker;
        this.embeddingClient = embeddingClient;
    }

    public List<EmbeddedTechnicalDocumentChunk> embedDocuments(List<TechnicalDocument> documents)
            throws Exception {

        if (documents == null) {
            throw new IllegalArgumentException("Technical documents list cannot be null");
        }

        if (documents.isEmpty()) {
            return List.of();
        }

        List<EmbeddedTechnicalDocumentChunk> embeddedChunks = new ArrayList<>();

        for (TechnicalDocument document : documents) {
            List<TechnicalDocumentChunk> chunks = chunker.chunkDocument(document);

            for (TechnicalDocumentChunk chunk : chunks) {
                List<Double> embedding = embeddingClient.embedText(chunk.content());

                embeddedChunks.add(new EmbeddedTechnicalDocumentChunk(
                        chunk,
                        embedding
                ));
            }
        }

        return embeddedChunks;
    }
}