package com.lucidia.lucidia.model;

import java.time.LocalDate;

public class DreamEntry {
    private int id;
    private int userId;
    private String dreamText;
    private LocalDate dreamDate;
    private int sleepQuality;
    private int lucidityLevel;

    // Constructors
    public DreamEntry() {}

    public DreamEntry(int userId, String dreamText, LocalDate dreamDate, int sleepQuality, int lucidityLevel) {
        this.userId = userId;
        this.dreamText = dreamText;
        this.dreamDate = dreamDate;
        this.sleepQuality = sleepQuality;
        this.lucidityLevel = lucidityLevel;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDreamText() { return dreamText; }
    public void setDreamText(String dreamText) { this.dreamText = dreamText; }

    public LocalDate getDreamDate() { return dreamDate; }
    public void setDreamDate(LocalDate dreamDate) { this.dreamDate = dreamDate; }

    public int getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(int sleepQuality) { this.sleepQuality = sleepQuality; }

    public int getLucidityLevel() { return lucidityLevel; }
    public void setLucidityLevel(int lucidityLevel) { this.lucidityLevel = lucidityLevel; }
}
