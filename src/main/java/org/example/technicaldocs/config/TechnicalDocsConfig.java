package org.example.technicaldocs.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class TechnicalDocsConfig {

    private static final String DEFAULT_RESOURCE_PATH = "docs/docs_config.json";

    private final String documentsPath;
    private final int maxChunkLengthChars;
    private final int fallbackOverlapChars;
    private final List<String> supportedExtensions;
    private final String embeddingModel;

    @JsonCreator
    public TechnicalDocsConfig(
            @JsonProperty("documentsPath") String documentsPath,
            @JsonProperty("maxChunkLengthChars") int maxChunkLengthChars,
            @JsonProperty("fallbackOverlapChars") int fallbackOverlapChars,
            @JsonProperty("supportedExtensions") List<String> supportedExtensions,
            @JsonProperty("embeddingModel") String embeddingModel
    ) {
        this.documentsPath = documentsPath;
        this.maxChunkLengthChars = maxChunkLengthChars;
        this.fallbackOverlapChars = fallbackOverlapChars;
        this.supportedExtensions = supportedExtensions;
        this.embeddingModel = embeddingModel;
    }

    public static TechnicalDocsConfig load() {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream inputStream = getResourceAsStream(DEFAULT_RESOURCE_PATH)) {
            return objectMapper.readValue(inputStream, TechnicalDocsConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load technical docs config from " + DEFAULT_RESOURCE_PATH,
                    e
            );
        }
    }

    private static InputStream getResourceAsStream(String resourcePath) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);

        if (Objects.isNull(inputStream)) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }

        return inputStream;
    }

    public String getDocumentsPath() {
        return documentsPath;
    }

    public int getMaxChunkLengthChars() {
        return maxChunkLengthChars;
    }

    public int getFallbackOverlapChars() {
        return fallbackOverlapChars;
    }

    public List<String> getSupportedExtensions() {
        return supportedExtensions;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }
}