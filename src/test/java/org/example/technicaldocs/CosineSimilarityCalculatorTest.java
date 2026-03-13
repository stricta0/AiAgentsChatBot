package org.example.technicaldocs;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CosineSimilarityCalculatorTest {

    private final CosineSimilarityCalculator calculator = new CosineSimilarityCalculator();

    @Test
    void shouldReturnOneForIdenticalVectors() {
        double similarity = calculator.calculate(
                List.of(1.0, 2.0, 3.0),
                List.of(1.0, 2.0, 3.0)
        );

        assertEquals(1.0, similarity, 1e-9);
    }

    @Test
    void shouldReturnZeroForOrthogonalVectors() {
        double similarity = calculator.calculate(
                List.of(1.0, 0.0),
                List.of(0.0, 1.0)
        );

        assertEquals(0.0, similarity, 1e-9);
    }

    @Test
    void shouldThrowForDifferentVectorSizes() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        List.of(1.0, 2.0),
                        List.of(1.0, 2.0, 3.0)
                )
        );

        assertEquals("Vectors must have the same size", exception.getMessage());
    }

    @Test
    void shouldThrowForZeroVector() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        List.of(0.0, 0.0),
                        List.of(1.0, 2.0)
                )
        );

        assertEquals("Vectors must not be zero vectors", exception.getMessage());
    }
}