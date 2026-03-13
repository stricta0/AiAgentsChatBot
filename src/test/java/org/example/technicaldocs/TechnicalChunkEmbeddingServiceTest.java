package org.example.technicaldocs;

import org.example.technicaldocs.model.EmbeddedTechnicalDocumentChunk;
import org.example.technicaldocs.model.TechnicalDocument;
import org.example.technicaldocs.model.TechnicalDocumentChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TechnicalChunkEmbeddingServiceTest {

    private TechnicalDocumentChunker chunker;
    private EmbeddingClient embeddingClient;
    private TechnicalChunkEmbeddingService service;

    @BeforeEach
    void setUp() {
        chunker = mock(TechnicalDocumentChunker.class);
        embeddingClient = mock(EmbeddingClient.class);
        service = new TechnicalChunkEmbeddingService(chunker, embeddingClient);
    }

    @Test
    void shouldChunkAndEmbedAllDocuments() throws Exception {
        TechnicalDocument doc1 = new TechnicalDocument("doc1.md", ".md", "content 1");
        TechnicalDocument doc2 = new TechnicalDocument("doc2.txt", ".txt", "content 2");

        TechnicalDocumentChunk chunk1 = new TechnicalDocumentChunk("doc1.md", ".md", "A", 0, "chunk 1");
        TechnicalDocumentChunk chunk2 = new TechnicalDocumentChunk("doc1.md", ".md", "B", 1, "chunk 2");
        TechnicalDocumentChunk chunk3 = new TechnicalDocumentChunk("doc2.txt", ".txt", "doc2.txt", 0, "chunk 3");

        when(chunker.chunkDocument(doc1)).thenReturn(List.of(chunk1, chunk2));
        when(chunker.chunkDocument(doc2)).thenReturn(List.of(chunk3));

        when(embeddingClient.embedText("chunk 1")).thenReturn(List.of(0.1, 0.2));
        when(embeddingClient.embedText("chunk 2")).thenReturn(List.of(0.3, 0.4));
        when(embeddingClient.embedText("chunk 3")).thenReturn(List.of(0.5, 0.6));

        List<EmbeddedTechnicalDocumentChunk> result = service.embedDocuments(List.of(doc1, doc2));

        assertEquals(3, result.size());

        assertEquals("doc1.md", result.get(0).chunk().documentName());
        assertEquals("chunk 1", result.get(0).chunk().content());
        assertEquals(List.of(0.1, 0.2), result.get(0).embedding());

        assertEquals("doc1.md", result.get(1).chunk().documentName());
        assertEquals("doc2.txt", result.get(2).chunk().documentName());

        verify(chunker).chunkDocument(doc1);
        verify(chunker).chunkDocument(doc2);
        verify(embeddingClient).embedText("chunk 1");
        verify(embeddingClient).embedText("chunk 2");
        verify(embeddingClient).embedText("chunk 3");
    }

    @Test
    void shouldReturnEmptyListForEmptyDocumentsList() throws Exception {
        List<EmbeddedTechnicalDocumentChunk> result = service.embedDocuments(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(chunker, embeddingClient);
    }

    @Test
    void shouldThrowWhenDocumentsListIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.embedDocuments(null)
        );

        assertEquals("Technical documents list cannot be null", exception.getMessage());
    }
}