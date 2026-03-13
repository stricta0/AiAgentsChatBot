package org.example.technicaldocs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.EnvConfig;
import org.example.technicaldocs.config.TechnicalDocsConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EmbeddingClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;
    private final String embeddingModel;

    public EmbeddingClient(TechnicalDocsConfig technicalDocsConfig) {
        this(
                HttpClient.newHttpClient(),
                new ObjectMapper(),
                EnvConfig.getGeminiApiKey(),
                EnvConfig.getGeminiApiUrl(),
                technicalDocsConfig.getEmbeddingModel()
        );
    }

    public EmbeddingClient(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            String apiKey,
            String apiUrl,
            String embeddingModel
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.embeddingModel = embeddingModel;

        validateConfig();
    }

    public List<Double> embedText(String text) throws IOException, InterruptedException {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text to embed must not be null or blank");
        }

        String requestBody = buildEmbedRequestBody(text);
        String rawResponse = sendEmbedRequest(requestBody);
        return extractEmbedding(rawResponse);
    }

    private String buildEmbedRequestBody(String text) throws IOException {
        JsonNode root = objectMapper.createObjectNode()
                .set("content", objectMapper.createObjectNode().set(
                        "parts",
                        objectMapper.createArrayNode().add(
                                objectMapper.createObjectNode().put("text", text)
                        )
                ));

        return objectMapper.writeValueAsString(root);
    }

    private String sendEmbedRequest(String requestBody) throws IOException, InterruptedException {
        String url = "%s/%s:embedContent".formatted(apiUrl, embeddingModel);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Gemini embedding API request failed. HTTP status: "
                            + response.statusCode() + ", body: " + response.body()
            );
        }

        return response.body();
    }

    private List<Double> extractEmbedding(String rawResponse) throws IOException {
        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode valuesNode = root.path("embedding").path("values");

        if (!valuesNode.isArray() || valuesNode.isEmpty()) {
            throw new IllegalStateException(
                    "Could not extract embedding values from Gemini response: " + rawResponse
            );
        }

        List<Double> result = new ArrayList<>();
        for (JsonNode valueNode : valuesNode) {
            result.add(valueNode.asDouble());
        }

        return result;
    }

    private void validateConfig() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing GEMINI_API_KEY");
        }

        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("Missing GEMINI_API_URL");
        }

        if (embeddingModel == null || embeddingModel.isBlank()) {
            throw new IllegalStateException("Missing embeddingModel in technical docs config");
        }
    }
}