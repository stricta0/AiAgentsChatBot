package org.example.router.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RoutingPlan {

    @JsonProperty("whole_original_message")
    private String wholeOriginalMessage;

    @JsonProperty("routing_notes")
    private String routingNotes;

    @JsonProperty("steps")
    private List<PlanStep> steps;

    public RoutingPlan() {
    }

    public String getWholeOriginalMessage() {
        return wholeOriginalMessage;
    }

    public void setWholeOriginalMessage(String wholeOriginalMessage) {
        this.wholeOriginalMessage = wholeOriginalMessage;
    }

    public String getRoutingNotes() {
        return routingNotes;
    }

    public void setRoutingNotes(String routingNotes) {
        this.routingNotes = routingNotes;
    }

    public List<PlanStep> getSteps() {
        return steps;
    }

    public void setSteps(List<PlanStep> steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "RoutingPlan{" +
                "wholeOriginalMessage='" + wholeOriginalMessage + '\'' +
                ", routingNotes='" + routingNotes + '\'' +
                ", steps=" + steps +
                '}';
    }

    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Original message:\n");
        sb.append(wholeOriginalMessage != null ? wholeOriginalMessage : "").append("\n\n");

        sb.append("Routing notes:\n");
        sb.append(routingNotes != null ? routingNotes : "").append("\n\n");

        sb.append("Steps:\n");

        if (steps == null || steps.isEmpty()) {
            sb.append("No steps.\n");
            return sb.toString();
        }

        for (int i = 0; i < steps.size(); i++) {
            PlanStep step = steps.get(i);

            sb.append(i + 1).append(". ")
                    .append(step.getAgent() != null ? step.getAgent() : "")
                    .append("\n");

            sb.append("   Task: ")
                    .append(step.getTask() != null ? step.getTask() : "")
                    .append("\n");

            sb.append("   Message section: \"")
                    .append(step.getOriginalMessageSection() != null ? step.getOriginalMessageSection() : "")
                    .append("\"\n");

            sb.append("   Confidence: ")
                    .append(step.getConfidence())
                    .append("\n\n");
        }

        return sb.toString();
    }

    public boolean hasUnknownSteps() {
        if (steps == null || steps.isEmpty()) {
            return false;
        }

        return steps.stream()
                .anyMatch(step -> "NONE".equalsIgnoreCase(step.getAgent()));
    }

    public List<PlanStep> getUnknownSteps() {
        if (steps == null || steps.isEmpty()) {
            return List.of();
        }

        return steps.stream()
                .filter(step -> "NONE".equalsIgnoreCase(step.getAgent()))
                .toList();
    }


}