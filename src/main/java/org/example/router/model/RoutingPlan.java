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
}