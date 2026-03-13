package org.example.technicaldocs;

import org.example.technicaldocs.model.EmbeddedTechnicalDocumentChunk;
import org.example.technicaldocs.model.TechnicalDocument;
import org.example.technicaldocs.model.TechnicalDocumentChunk;
import org.example.technicaldocs.model.TechnicalSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TechnicalDocumentationServiceTest {

    private TechnicalDocumentLoader documentLoader;
    private TechnicalChunkEmbeddingService chunkEmbeddingService;
    private TechnicalChunkRetriever chunkRetriever;
    private EmbeddingClient embeddingClient;
    private TechnicalDocumentationService service;

    @BeforeEach
    void setUp() {
        documentLoader = mock(TechnicalDocumentLoader.class);
        chunkEmbeddingService = mock(TechnicalChunkEmbeddingService.class);
        chunkRetriever = mock(TechnicalChunkRetriever.class);
        embeddingClient = mock(EmbeddingClient.class);

        service = new TechnicalDocumentationService(
                documentLoader,
                chunkEmbeddingService,
                chunkRetriever,
                embeddingClient
        );
    }

    @Test
    void shouldInitializeAndPreloadEmbeddedChunks() throws Exception {
        TechnicalDocument document = new TechnicalDocument("doc.md", ".md", "content");
        EmbeddedTechnicalDocumentChunk embeddedChunk = new EmbeddedTechnicalDocumentChunk(
                new TechnicalDocumentChunk("doc.md", ".md", "Heading", 0, "chunk"),
                List.of(0.1, 0.2)
        );

        when(documentLoader.loadDocuments()).thenReturn(List.of(document));
        when(chunkEmbeddingService.embedDocuments(List.of(document))).thenReturn(List.of(embeddedChunk));

        service.initialize();

        assertTrue(service.isInitialized());
        assertEquals(1, service.getEmbeddedChunkCount());

        verify(documentLoader).loadDocuments();
        verify(chunkEmbeddingService).embedDocuments(List.of(document));
    }

    @Test
    void shouldInitializeOnlyOnce() throws Exception {
        TechnicalDocument document = new TechnicalDocument("doc.md", ".md", "content");
        EmbeddedTechnicalDocumentChunk embeddedChunk = new EmbeddedTechnicalDocumentChunk(
                new TechnicalDocumentChunk("doc.md", ".md", "Heading", 0, "chunk"),
                List.of(0.1, 0.2)
        );

        when(documentLoader.loadDocuments()).thenReturn(List.of(document));
        when(chunkEmbeddingService.embedDocuments(List.of(document))).thenReturn(List.of(embeddedChunk));

        service.initialize();
        service.initialize();

        verify(documentLoader, times(1)).loadDocuments();
        verify(chunkEmbeddingService, times(1)).embedDocuments(List.of(document));
    }

    @Test
    void shouldReturnRelevantChunksForQuery() throws Exception {
        TechnicalDocument document = new TechnicalDocument("doc.md", ".md", "content");
        EmbeddedTechnicalDocumentChunk embeddedChunk = new EmbeddedTechnicalDocumentChunk(
                new TechnicalDocumentChunk("doc.md", ".md", "Heading", 0, "chunk"),
                List.of(0.1, 0.2)
        );

        TechnicalSearchResult searchResult = new TechnicalSearchResult(
                embeddedChunk.chunk(),
                0.95
        );

        when(documentLoader.loadDocuments()).thenReturn(List.of(document));
        when(chunkEmbeddingService.embedDocuments(List.of(document))).thenReturn(List.of(embeddedChunk));
        when(embeddingClient.embedText("How do webhooks work?")).thenReturn(List.of(0.9, 0.8));
        when(chunkRetriever.findTopMatches(List.of(0.9, 0.8), List.of(embeddedChunk), 3))
                .thenReturn(List.of(searchResult));

        service.initialize();
        List<TechnicalSearchResult> results = service.findRelevantChunks("How do webhooks work?", 3);

        assertEquals(1, results.size());
        assertEquals("doc.md", results.get(0).chunk().documentName());
        assertEquals(0.95, results.get(0).similarityScore());

        verify(embeddingClient).embedText("How do webhooks work?");
        verify(chunkRetriever).findTopMatches(List.of(0.9, 0.8), List.of(embeddedChunk), 3);
    }

    @Test
    void shouldThrowWhenFindRelevantChunksCalledBeforeInitialization() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.findRelevantChunks("test", 3)
        );

        assertEquals("TechnicalDocumentationService is not initialized", exception.getMessage());
    }

    @Test
    void shouldThrowWhenQueryIsBlank() throws Exception {
        when(documentLoader.loadDocuments()).thenReturn(List.of());
        when(chunkEmbeddingService.embedDocuments(List.of())).thenReturn(List.of());

        service.initialize();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.findRelevantChunks("   ", 3)
        );

        assertEquals("Query cannot be null or blank", exception.getMessage());
    }
}