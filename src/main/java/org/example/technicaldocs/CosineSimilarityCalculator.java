package org.example.technicaldocs;

import java.util.List;

public class CosineSimilarityCalculator {

    public double calculate(List<Double> left, List<Double> right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Vectors cannot be null");
        }

        if (left.isEmpty() || right.isEmpty()) {
            throw new IllegalArgumentException("Vectors cannot be empty");
        }

        if (left.size() != right.size()) {
            throw new IllegalArgumentException("Vectors must have the same size");
        }

        double dotProduct = 0.0;
        double leftNorm = 0.0;
        double rightNorm = 0.0;

        for (int i = 0; i < left.size(); i++) {
            double l = left.get(i);
            double r = right.get(i);

            dotProduct += l * r;
            leftNorm += l * l;
            rightNorm += r * r;
        }

        if (leftNorm == 0.0 || rightNorm == 0.0) {
            throw new IllegalArgumentException("Vectors must not be zero vectors");
        }

        return dotProduct / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }
}