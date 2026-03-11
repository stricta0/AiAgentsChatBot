package org.example.config;

import io.github.cdimascio.dotenv.Dotenv;


public class EnvConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static String getGeminiApiKey() {
        return dotenv.get("GEMINI_API_KEY");
    }

    public static String getGeminiModel() {
        return dotenv.get("GEMINI_MODEL");
    }

    public static String getGeminiApiUrl() {
        return dotenv.get("GEMINI_API_URL");
    }
}