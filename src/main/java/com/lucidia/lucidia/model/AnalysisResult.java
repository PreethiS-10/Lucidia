package com.lucidia.lucidia.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AnalysisResult {
    private int id;
    private int dreamId;
    private double emotionScore;
    private String dominantEmotion;
    private List<String> symbolsDetected;
    private String interpretationText;
    private double confidenceScore;
    private LocalDateTime analysisTimestamp;
    private Map<String, Double> emotionBreakdown;

    // Constructors
    public AnalysisResult() {
        this.analysisTimestamp = LocalDateTime.now();
        this.confidenceScore = 0.0;
    }


    // In AnalysisResult class, update the constructor:
    public AnalysisResult(Map<String, Double> emotions, List<String> symbols, String interpretation) {
        this();
        this.emotionBreakdown = emotions;
        this.symbolsDetected = symbols;
        this.interpretationText = interpretation;

        // Find dominant emotion
        this.dominantEmotion = emotions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");

        this.emotionScore = emotions.getOrDefault(dominantEmotion, 0.0);
        this.confidenceScore = calculateConfidence(emotions, symbols);
    }
    private double calculateConfidence(Map<String, Double> emotions, List<String> symbols) {
        double maxEmotion = emotions.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double symbolConfidence = Math.min(symbols.size() * 0.2, 1.0);
        return (maxEmotion + symbolConfidence) / 2.0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDreamId() { return dreamId; }
    public void setDreamId(int dreamId) { this.dreamId = dreamId; }

    public double getEmotionScore() { return emotionScore; }
    public void setEmotionScore(double emotionScore) { this.emotionScore = emotionScore; }

    public String getDominantEmotion() { return dominantEmotion; }
    public void setDominantEmotion(String dominantEmotion) { this.dominantEmotion = dominantEmotion; }

    public List<String> getSymbolsDetected() { return symbolsDetected; }
    public void setSymbolsDetected(List<String> symbolsDetected) { this.symbolsDetected = symbolsDetected; }

    public String getInterpretationText() { return interpretationText; }
    public void setInterpretationText(String interpretationText) { this.interpretationText = interpretationText; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public LocalDateTime getAnalysisTimestamp() { return analysisTimestamp; }
    public void setAnalysisTimestamp(LocalDateTime analysisTimestamp) { this.analysisTimestamp = analysisTimestamp; }

    public Map<String, Double> getEmotionBreakdown() { return emotionBreakdown; }
    public void setEmotionBreakdown(Map<String, Double> emotionBreakdown) { this.emotionBreakdown = emotionBreakdown; }
}
