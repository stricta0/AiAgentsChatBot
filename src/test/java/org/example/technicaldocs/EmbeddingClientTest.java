package org.example.technicaldocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmbeddingClientTest {

    @Test
    void shouldExtractEmbeddingFromSuccessfulResponse() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mockStringResponse();

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
                {
                  "embedding": {
                    "values": [0.1, 0.2, 0.3]
                  }
                }
                """);

        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        EmbeddingClient client = new EmbeddingClient(
                httpClient,
                new ObjectMapper(),
                "test-api-key",
                "https://generativelanguage.googleapis.com/v1beta",
                "models/gemini-embedding-001"
        );

        List<Double> embedding = client.embedText("hello world");

        assertEquals(List.of(0.1, 0.2, 0.3), embedding);
    }

    @Test
    void shouldThrowWhenTextIsBlank() {
        EmbeddingClient client = new EmbeddingClient(
                mock(HttpClient.class),
                new ObjectMapper(),
                "test-api-key",
                "https://generativelanguage.googleapis.com/v1beta",
                "models/gemini-embedding-001"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> client.embedText("   ")
        );

        assertEquals("Text to embed must not be null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowWhenApiReturnsNonSuccessStatus() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mockStringResponse();

        when(response.statusCode()).thenReturn(429);
        when(response.body()).thenReturn("{\"error\":\"quota exceeded\"}");

        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        EmbeddingClient client = new EmbeddingClient(
                httpClient,
                new ObjectMapper(),
                "test-api-key",
                "https://generativelanguage.googleapis.com/v1beta",
                "models/gemini-embedding-001"
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> client.embedText("hello world")
        );

        assertTrue(exception.getMessage().contains("Gemini embedding API request failed"));
        assertTrue(exception.getMessage().contains("429"));
    }

    @Test
    void shouldThrowWhenEmbeddingValuesAreMissing() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mockStringResponse();

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
                {
                  "embedding": {}
                }
                """);

        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        EmbeddingClient client = new EmbeddingClient(
                httpClient,
                new ObjectMapper(),
                "test-api-key",
                "https://generativelanguage.googleapis.com/v1beta",
                "models/gemini-embedding-001"
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> client.embedText("hello world")
        );

        assertTrue(exception.getMessage().contains("Could not extract embedding values"));
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockStringResponse() {
        return (HttpResponse<String>) mock(HttpResponse.class);
    }
}