package org.example.technicaldocs;

import org.example.technicaldocs.model.EmbeddedTechnicalDocumentChunk;
import org.example.technicaldocs.model.TechnicalDocumentChunk;
import org.example.technicaldocs.model.TechnicalSearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalChunkRetrieverTest {

    @Test
    void shouldReturnTopMatchesSortedBySimilarity() {
        TechnicalChunkRetriever retriever =
                new TechnicalChunkRetriever(new CosineSimilarityCalculator());

        EmbeddedTechnicalDocumentChunk chunk1 = new EmbeddedTechnicalDocumentChunk(
                new TechnicalDocumentChunk("doc1.md", ".md", "A", 0, "chunk 1"),
                List.of(1.0, 0.0)
        );

        EmbeddedTechnicalDocumentChunk chunk2 = new EmbeddedTechnicalDocumentChunk(
                new TechnicalDocumentChunk("doc2.md", ".md", "B", 1, "chunk 2"),
                List.of(0.0, 1.0)
        );

        EmbeddedTechnicalDocumentChunk chunk3 = new EmbeddedTechnicalDocumentChunk(
                new TechnicalDocumentChunk("doc3.md", ".md", "C", 2, "chunk 3"),
                List.of(0.8, 0.2)
        );

        List<TechnicalSearchResult> results = retriever.findTopMatches(
                List.of(1.0, 0.0),
                List.of(chunk1, chunk2, chunk3),
                2
        );

        assertEquals(2, results.size());
        assertEquals("doc1.md", results.get(0).chunk().documentName());
        assertEquals("doc3.md", results.get(1).chunk().documentName());
        assertTrue(results.get(0).similarityScore() >= results.get(1).similarityScore());
    }

    @Test
    void shouldThrowWhenTopKIsInvalid() {
        TechnicalChunkRetriever retriever =
                new TechnicalChunkRetriever(new CosineSimilarityCalculator());

        EmbeddedTechnicalDocumentChunk chunk = new EmbeddedTechnicalDocumentChunk(
                new TechnicalDocumentChunk("doc1.md", ".md", "A", 0, "chunk 1"),
                List.of(1.0, 0.0)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> retriever.findTopMatches(
                        List.of(1.0, 0.0),
                        List.of(chunk),
                        0
                )
        );

        assertEquals("topK must be greater than 0", exception.getMessage());
    }
}