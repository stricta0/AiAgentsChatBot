package org.example.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.EnvConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class LlmClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final String apiKey;
    private final String model;
    private final String apiUrl;

    public LlmClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        this.apiKey = EnvConfig.getGeminiApiKey();
        this.model = EnvConfig.getGeminiModel();
        this.apiUrl = EnvConfig.getGeminiApiUrl();

        validateConfig();
    }

    public String getResponseFromGemini(String prompt) throws IOException, InterruptedException {
        String requestBody = buildGenerateContentRequestBody(prompt);
        String rawResponse = sendGenerateContentRequest(requestBody);
        return extractTextFromResponse(rawResponse);
    }

    private String buildGenerateContentRequestBody(String prompt) throws IOException {
        JsonNode root = objectMapper.createObjectNode()
                .set("contents", objectMapper.createArrayNode().add(
                        objectMapper.createObjectNode().set("parts", objectMapper.createArrayNode().add(
                                objectMapper.createObjectNode().put("text", prompt)
                        ))
                ));

        return objectMapper.writeValueAsString(root);
    }

    private String sendGenerateContentRequest(String requestBody) throws IOException, InterruptedException {
        String url = "%s/%s:generateContent".formatted(apiUrl, model);

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
                    "Gemini API request failed. HTTP status: " + response.statusCode() +
                            ", body: " + response.body()
            );
        }

        return response.body();
    }

    private String extractTextFromResponse(String rawResponse) throws IOException {
        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode textNode = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.isNull()) {
            throw new IllegalStateException("Could not extract text from Gemini response: " + rawResponse);
        }

        return textNode.asText();
    }

    private void validateConfig() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing GEMINI_API_KEY");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalStateException("Missing GEMINI_MODEL");
        }
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("Missing GEMINI_API_URL");
        }
    }
}