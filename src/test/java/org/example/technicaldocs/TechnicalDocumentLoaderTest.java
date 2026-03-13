package org.example.technicaldocs;

import org.example.technicaldocs.config.TechnicalDocsConfig;
import org.example.technicaldocs.model.TechnicalDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalDocumentLoaderTest {

    @Test
    void shouldLoadOnlySupportedDocumentsFromResources() {
        TechnicalDocsConfig config = TechnicalDocsConfig.load();
        TechnicalDocumentLoader loader = new TechnicalDocumentLoader(config);

        List<TechnicalDocument> documents = loader.loadDocuments();

        assertEquals(4, documents.size());

        Set<String> names = documents.stream()
                .map(TechnicalDocument::getDocumentName)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(names.contains("api_reference.txt"));
        assertTrue(names.contains("deployment.md"));
        assertTrue(names.contains("integration.md"));
        assertTrue(names.contains("troubleshooting.md"));

        assertTrue(documents.stream().allMatch(document -> !document.getContent().isBlank()));
        assertTrue(documents.stream().allMatch(document ->
                config.getSupportedExtensions().contains(document.getExtension())
        ));
    }

    @Test
    void shouldPrintWarningForUnsupportedFiles() {
        TechnicalDocsConfig config = TechnicalDocsConfig.load();
        TechnicalDocumentLoader loader = new TechnicalDocumentLoader(config);

        PrintStream originalErr = System.err;
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();

        try {
            System.setErr(new PrintStream(errorOutput));

            List<TechnicalDocument> documents = loader.loadDocuments();

            assertEquals(4, documents.size());

            String stderr = errorOutput.toString();
            assertTrue(stderr.contains("Warning: Unsupported technical document skipped: bad_doc.json"));

        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void shouldThrowWhenDocumentsPathDoesNotExist() {
        TechnicalDocsConfig config = new TechnicalDocsConfig(
                "docs/missing-folder",
                3500,
                300,
                List.of(".md", ".txt"),
                "models/gemini-embedding-001"
        );

        TechnicalDocumentLoader loader = new TechnicalDocumentLoader(config);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                loader::loadDocuments
        );

        assertTrue(exception.getMessage().contains("Failed to resolve technical documents resource path"));
    }
}