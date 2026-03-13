package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class JsonResourceLoader {

    private final ObjectMapper objectMapper;

    public JsonResourceLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T load(String resourcePath, Class<T> type) {
        try (InputStream inputStream = getResourceAsStream(resourcePath)) {
            return objectMapper.readValue(inputStream, type);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + resourcePath, e);
        }
    }

    private InputStream getResourceAsStream(String resourcePath) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);

        if (Objects.isNull(inputStream)) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }

        return inputStream;
    }
}