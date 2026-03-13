package org.example.technicaldocs;

import org.example.technicaldocs.config.TechnicalDocsConfig;
import org.example.technicaldocs.model.TechnicalDocument;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TechnicalDocumentLoader {

    private final TechnicalDocsConfig config;

    public TechnicalDocumentLoader(TechnicalDocsConfig config) {
        this.config = config;
    }

    public List<TechnicalDocument> loadDocuments() {
        validateConfig();

        Path rootPath = resolveDocumentsRoot(config.getDocumentsPath());

        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .map(this::loadDocumentIfSupported)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load technical documents from path: " + config.getDocumentsPath(),
                    e
            );
        }
    }

    private TechnicalDocument loadDocumentIfSupported(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = extractExtension(fileName);

        if (!config.getSupportedExtensions().contains(extension)) {
            System.err.println("Warning: Unsupported technical document skipped: " + fileName);
            return null;
        }

        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);

            return new TechnicalDocument(
                    fileName,
                    extension,
                    content
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read technical document: " + fileName,
                    e
            );
        }
    }

    private Path resolveDocumentsRoot(String resourcePath) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URI uri = Objects.requireNonNull(
                    classLoader.getResource(resourcePath),
                    "Resource not found: " + resourcePath
            ).toURI();

            if ("jar".equalsIgnoreCase(uri.getScheme())) {
                FileSystem fileSystem;
                try {
                    fileSystem = FileSystems.getFileSystem(uri);
                } catch (Exception ignored) {
                    fileSystem = FileSystems.newFileSystem(uri, java.util.Map.of());
                }
                return fileSystem.getPath(resourcePath);
            }

            return Path.of(uri);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to resolve technical documents resource path: " + resourcePath,
                    e
            );
        }
    }

    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex).toLowerCase();
    }

    private void validateConfig() {
        if (config == null) {
            throw new IllegalArgumentException("Technical docs config cannot be null");
        }

        if (config.getDocumentsPath() == null || config.getDocumentsPath().isBlank()) {
            throw new IllegalArgumentException("Documents path cannot be null or blank");
        }

        if (config.getSupportedExtensions() == null || config.getSupportedExtensions().isEmpty()) {
            throw new IllegalArgumentException("Supported extensions cannot be null or empty");
        }
    }
}