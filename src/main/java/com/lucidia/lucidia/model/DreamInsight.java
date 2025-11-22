package com.lucidia.lucidia.model;

import java.util.List;

public class DreamInsight {
    private String title;
    private String description;
    private InsightType type;
    private String recommendation;

    public enum InsightType {
        PATTERN, ACHIEVEMENT, SUGGESTION, TREND
    }

    // Constructors
    public DreamInsight() {}

    public DreamInsight(String title, String description, InsightType type, String recommendation) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.recommendation = recommendation;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public InsightType getType() { return type; }
    public void setType(InsightType type) { this.type = type; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}