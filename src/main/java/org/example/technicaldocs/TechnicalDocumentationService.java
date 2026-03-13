package org.example.technicaldocs;

import org.example.technicaldocs.model.EmbeddedTechnicalDocumentChunk;
import org.example.technicaldocs.model.TechnicalDocument;
import org.example.technicaldocs.model.TechnicalSearchResult;

import java.util.List;

public class TechnicalDocumentationService {

    private final TechnicalDocumentLoader documentLoader;
    private final TechnicalChunkEmbeddingService chunkEmbeddingService;
    private final TechnicalChunkRetriever chunkRetriever;
    private final EmbeddingClient embeddingClient;

    private List<EmbeddedTechnicalDocumentChunk> embeddedChunks;
    private boolean initialized;

    public TechnicalDocumentationService(
            TechnicalDocumentLoader documentLoader,
            TechnicalChunkEmbeddingService chunkEmbeddingService,
            TechnicalChunkRetriever chunkRetriever,
            EmbeddingClient embeddingClient
    ) {
        this.documentLoader = documentLoader;
        this.chunkEmbeddingService = chunkEmbeddingService;
        this.chunkRetriever = chunkRetriever;
        this.embeddingClient = embeddingClient;
        this.embeddedChunks = List.of();
        this.initialized = false;
    }

    public void initialize() throws Exception {
        if (initialized) {
            return;
        }

        List<TechnicalDocument> documents = documentLoader.loadDocuments();
        this.embeddedChunks = chunkEmbeddingService.embedDocuments(documents);
        this.initialized = true;
    }

    public List<TechnicalSearchResult> findRelevantChunks(String query, int topK) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("TechnicalDocumentationService is not initialized");
        }

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }

        List<Double> queryEmbedding = embeddingClient.embedText(query.trim());

        return chunkRetriever.findTopMatches(
                queryEmbedding,
                embeddedChunks,
                topK
        );
    }

    public boolean isInitialized() {
        return initialized;
    }

    public int getEmbeddedChunkCount() {
        return embeddedChunks.size();
    }
}