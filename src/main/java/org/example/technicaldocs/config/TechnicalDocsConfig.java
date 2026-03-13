package org.example.technicaldocs.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TechnicalDocsConfig {

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