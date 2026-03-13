package org.example.technicaldocs;

import org.example.technicaldocs.model.EmbeddedTechnicalDocumentChunk;
import org.example.technicaldocs.model.TechnicalSearchResult;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TechnicalChunkRetriever {

    private final CosineSimilarityCalculator cosineSimilarityCalculator;

    public TechnicalChunkRetriever(CosineSimilarityCalculator cosineSimilarityCalculator) {
        this.cosineSimilarityCalculator = cosineSimilarityCalculator;
    }

    public List<TechnicalSearchResult> findTopMatches(
            List<Double> queryEmbedding,
            List<EmbeddedTechnicalDocumentChunk> embeddedChunks,
            int topK
    ) {
        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            throw new IllegalArgumentException("Query embedding cannot be null or empty");
        }

        if (embeddedChunks == null || embeddedChunks.isEmpty()) {
            throw new IllegalArgumentException("Embedded chunks cannot be null or empty");
        }

        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than 0");
        }

        return embeddedChunks.stream()
                .map(chunk -> new TechnicalSearchResult(
                        chunk.chunk(),
                        cosineSimilarityCalculator.calculate(queryEmbedding, chunk.embedding())
                ))
                .sorted(Comparator.comparingDouble(TechnicalSearchResult::similarityScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }
}