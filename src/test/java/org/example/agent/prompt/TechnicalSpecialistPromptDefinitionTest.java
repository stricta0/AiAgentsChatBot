package org.example.agent.prompt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalSpecialistPromptDefinitionTest {

    @Test
    void shouldLoadTechnicalSpecialistPromptDefinitionFromResources() {
        TechnicalSpecialistPromptDefinition definition = TechnicalSpecialistPromptDefinition.load();

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