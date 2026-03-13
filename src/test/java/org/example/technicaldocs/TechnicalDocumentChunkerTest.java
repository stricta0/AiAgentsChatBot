package org.example.technicaldocs;

import org.example.technicaldocs.config.TechnicalDocsConfig;
import org.example.technicaldocs.model.TechnicalDocument;
import org.example.technicaldocs.model.TechnicalDocumentChunk;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalDocumentChunkerTest {

    @Test
    void shouldLoadConfigFromResources() {
        TechnicalDocsConfig config = TechnicalDocsConfig.load();

        assertEquals("docs/sources", config.getDocumentsPath());
        assertEquals(3500, config.getMaxChunkLengthChars());
        assertEquals(300, config.getFallbackOverlapChars());
        assertEquals("models/gemini-embedding-001", config.getEmbeddingModel());
        assertTrue(config.getSupportedExtensions().contains(".md"));
        assertTrue(config.getSupportedExtensions().contains(".txt"));
    }

    @Test
    void shouldChunkMarkdownDocumentUsingHeadings() {
        TechnicalDocsConfig config = TechnicalDocsConfig.load();
        TechnicalDocumentChunker chunker = new TechnicalDocumentChunker(config);

        TechnicalDocument document = loadResourceDocument(
                "docs/sources/troubleshooting.md",
                "troubleshooting.md",
                ".md"
        );

        List<TechnicalDocumentChunk> chunks = chunker.chunkDocument(document);

        assertFalse(chunks.isEmpty());

        assertTrue(chunks.stream().allMatch(chunk ->
                chunk.content().length() <= config.getMaxChunkLengthChars()
        ));

        assertTrue(chunks.stream().anyMatch(chunk ->
                chunk.headingPath().contains("Troubleshooting Guide")
        ));

        assertTrue(chunks.stream().anyMatch(chunk ->
                chunk.content().contains("HTTP 401 Unauthorized")
        ));

        assertTrue(chunks.stream().anyMatch(chunk ->
                chunk.content().contains("Webhook Delivery Failures")
        ));
    }

    @Test
    void shouldSplitMarkdownMoreAggressivelyWhenChunkLimitIsSmall() {
        TechnicalDocsConfig smallConfig = new TechnicalDocsConfig(
                "docs/sources",
                450,
                80,
                List.of(".md", ".txt"),
                "models/gemini-embedding-001"
        );

        TechnicalDocumentChunker chunker = new TechnicalDocumentChunker(smallConfig);

        TechnicalDocument document = loadResourceDocument(
                "docs/sources/troubleshooting.md",
                "troubleshooting.md",
                ".md"
        );

        List<TechnicalDocumentChunk> chunks = chunker.chunkDocument(document);

        assertFalse(chunks.isEmpty());

        assertTrue(chunks.stream().allMatch(chunk ->
                chunk.content().length() <= smallConfig.getMaxChunkLengthChars()
        ));

        assertTrue(chunks.size() >= 6);

        assertTrue(chunks.stream().anyMatch(chunk ->
                chunk.headingPath().contains("HTTP 401 Unauthorized")
                        || chunk.content().contains("HTTP 401 Unauthorized")
        ));
    }

    @Test
    void shouldChunkTxtDocumentUsingFallbackLogic() {
        TechnicalDocsConfig smallConfig = new TechnicalDocsConfig(
                "docs/sources",
                220,
                40,
                List.of(".md", ".txt"),
                "models/gemini-embedding-001"
        );

        TechnicalDocumentChunker chunker = new TechnicalDocumentChunker(smallConfig);

        TechnicalDocument document = loadResourceDocument(
                "docs/sources/api_reference.txt",
                "api_reference.txt",
                ".txt"
        );

        List<TechnicalDocumentChunk> chunks = chunker.chunkDocument(document);

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.size() >= 2);

        assertTrue(chunks.stream().allMatch(chunk ->
                chunk.content().length() <= smallConfig.getMaxChunkLengthChars()
        ));

        assertTrue(chunks.stream().allMatch(chunk ->
                chunk.headingPath().equals("api_reference.txt")
        ));

        assertTrue(chunks.stream().anyMatch(chunk ->
                chunk.content().contains("401 Unauthorized")
        ));
    }

    @Test
    void shouldFallbackToParagraphOrSentenceSplittingWhenMarkdownSectionHasNoSubheadings() {
        TechnicalDocsConfig smallConfig = new TechnicalDocsConfig(
                "docs/sources",
                180,
                30,
                List.of(".md", ".txt"),
                "models/gemini-embedding-001"
        );

        TechnicalDocumentChunker chunker = new TechnicalDocumentChunker(smallConfig);

        TechnicalDocument document = loadResourceDocument(
                "docs/sources/deployment.md",
                "deployment.md",
                ".md"
        );

        List<TechnicalDocumentChunk> chunks = chunker.chunkDocument(document);

        assertFalse(chunks.isEmpty());

        assertTrue(chunks.stream().allMatch(chunk ->
                chunk.content().length() <= smallConfig.getMaxChunkLengthChars()
        ));

        assertTrue(chunks.stream().anyMatch(chunk ->
                chunk.headingPath().contains("Deployment Guide > Logging")
                        || chunk.content().contains("Logging")
        ));
    }

    @Test
    void shouldThrowForUnsupportedExtension() {
        TechnicalDocsConfig config = TechnicalDocsConfig.load();
        TechnicalDocumentChunker chunker = new TechnicalDocumentChunker(config);

        TechnicalDocument badDocument = new TechnicalDocument(
                "bad_doc.json",
                ".json",
                "{\"example\":\"test\"}"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chunker.chunkDocument(badDocument)
        );

        assertEquals(
                "Unsupported technical document extension: .json",
                exception.getMessage()
        );
    }

    @Test
    void shouldPreserveChunkMetadata() {
        TechnicalDocsConfig config = TechnicalDocsConfig.load();
        TechnicalDocumentChunker chunker = new TechnicalDocumentChunker(config);

        TechnicalDocument document = loadResourceDocument(
                "docs/sources/integration.md",
                "integration.md",
                ".md"
        );

        List<TechnicalDocumentChunk> chunks = chunker.chunkDocument(document);

        assertFalse(chunks.isEmpty());

        for (int i = 0; i < chunks.size(); i++) {
            TechnicalDocumentChunk chunk = chunks.get(i);

            assertEquals("integration.md", chunk.documentName());
            assertEquals(".md", chunk.documentType());
            assertNotNull(chunk.headingPath());
            assertFalse(chunk.headingPath().isBlank());
            assertEquals(i, chunk.chunkIndex());
            assertNotNull(chunk.content());
            assertFalse(chunk.content().isBlank());
            assertTrue(chunk.content().startsWith("Heading Path: "));
        }
    }

    private TechnicalDocument loadResourceDocument(String resourcePath, String fileName, String extension) {
        try (InputStream inputStream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                throw new IllegalStateException("Resource not found in test: " + resourcePath);
            }

            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return new TechnicalDocument(fileName, extension, content);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load test resource: " + resourcePath, e);
        }
    }
}