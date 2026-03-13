package org.example.agent.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.JsonResourceLoader;
import org.example.config.ResourcePaths;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalSpecialistPromptDefinitionTest {

    @Test
    void shouldLoadTechnicalSpecialistPromptDefinitionFromResources() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonResourceLoader jsonResourceLoader = new JsonResourceLoader(objectMapper);

        TechnicalSpecialistPromptDefinition definition =
                jsonResourceLoader.load(
                        ResourcePaths.TECHNICAL_SPECIALIST_PROMPT,
                        TechnicalSpecialistPromptDefinition.class
                );

        assertEquals(3, definition.getTopK());
        assertNotNull(definition.getTaskLines());
        assertFalse(definition.getTaskLines().isEmpty());
        assertNotNull(definition.getRules());
        assertFalse(definition.getRules().isEmpty());
        assertNotNull(definition.getOutputFormat());
        assertTrue(definition.getOutputFormat().contains("SUCCESS"));
        assertTrue(definition.getOutputFormat().contains("message_to_user"));
    }
}