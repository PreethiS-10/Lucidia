package com.lucidia.lucidia.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DreamStatistics {
    private int totalDreams;
    private int dreamsThisMonth;
    private int dreamsThisWeek;
    private double averageSleepQuality;
    private double averageLucidity;
    private String mostCommonEmotion;
    private List<String> topSymbols;
    private Map<LocalDate, Integer> dreamsPerDay;
    private Map<String, Integer> emotionFrequency;
    private Map<String, Integer> symbolFrequency;
    private int longestDreamStreak;
    private int currentStreak;

    // Constructors
    public DreamStatistics() {}

    // Getters and Setters
    public int getTotalDreams() { return totalDreams; }
    public void setTotalDreams(int totalDreams) { this.totalDreams = totalDreams; }

    public int getDreamsThisMonth() { return dreamsThisMonth; }
    public void setDreamsThisMonth(int dreamsThisMonth) { this.dreamsThisMonth = dreamsThisMonth; }

    public int getDreamsThisWeek() { return dreamsThisWeek; }
    public void setDreamsThisWeek(int dreamsThisWeek) { this.dreamsThisWeek = dreamsThisWeek; }

    public double getAverageSleepQuality() { return averageSleepQuality; }
    public void setAverageSleepQuality(double averageSleepQuality) { this.averageSleepQuality = averageSleepQuality; }

    public double getAverageLucidity() { return averageLucidity; }
    public void setAverageLucidity(double averageLucidity) { this.averageLucidity = averageLucidity; }

    public String getMostCommonEmotion() { return mostCommonEmotion; }
    public void setMostCommonEmotion(String mostCommonEmotion) { this.mostCommonEmotion = mostCommonEmotion; }

    public List<String> getTopSymbols() { return topSymbols; }
    public void setTopSymbols(List<String> topSymbols) { this.topSymbols = topSymbols; }

    public Map<LocalDate, Integer> getDreamsPerDay() { return dreamsPerDay; }
    public void setDreamsPerDay(Map<LocalDate, Integer> dreamsPerDay) { this.dreamsPerDay = dreamsPerDay; }

    public Map<String, Integer> getEmotionFrequency() { return emotionFrequency; }
    public void setEmotionFrequency(Map<String, Integer> emotionFrequency) { this.emotionFrequency = emotionFrequency; }

    public Map<String, Integer> getSymbolFrequency() { return symbolFrequency; }
    public void setSymbolFrequency(Map<String, Integer> symbolFrequency) { this.symbolFrequency = symbolFrequency; }

    public int getLongestDreamStreak() { return longestDreamStreak; }
    public void setLongestDreamStreak(int longestDreamStreak) { this.longestDreamStreak = longestDreamStreak; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
}