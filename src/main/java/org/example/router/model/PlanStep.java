package org.example.router.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlanStep {

    @JsonProperty("agent")
    private String agent;

    @JsonProperty("task")
    private String task;

    @JsonProperty("original_message_section")
    private String originalMessageSection;

    @JsonProperty("additional_context")
    private String additionalContext;

    @JsonProperty("confidence")
    private double confidence;

    public PlanStep() {
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getOriginalMessageSection() {
        return originalMessageSection;
    }

    public void setOriginalMessageSection(String originalMessageSection) {
        this.originalMessageSection = originalMessageSection;
    }

    public String getAdditionalContext() {
        return additionalContext;
    }

    public void setAdditionalContext(String additionalContext) {
        this.additionalContext = additionalContext;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "PlanStep{" +
                "agent='" + agent + '\'' +
                ", task='" + task + '\'' +
                ", originalMessageSection='" + originalMessageSection + '\'' +
                ", additionalContext='" + additionalContext + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}