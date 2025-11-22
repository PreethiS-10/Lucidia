package com.lucidia.lucidia.service;

import com.lucidia.lucidia.model.DreamEntry;
import com.lucidia.lucidia.model.DreamInsight;
import com.lucidia.lucidia.model.DreamStatistics;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class DreamAnalyticsService {
    private final DatabaseService databaseService;
    private final NLPService nlpService;

    public DreamAnalyticsService() {
        this.databaseService = DatabaseService.getInstance();
        this.nlpService = new NLPService();
    }

    public DreamStatistics generateUserStatistics(int userId) {
        try {
            List<DreamEntry> allDreams = databaseService.getAllDreams(userId);
            DreamStatistics stats = new DreamStatistics();

            if (allDreams.isEmpty()) {
                return initializeEmptyStats();
            }

            // Basic counts
            stats.setTotalDreams(allDreams.size());
            stats.setDreamsThisMonth(countDreamsThisMonth(allDreams));
            stats.setDreamsThisWeek(countDreamsThisWeek(allDreams));

            // Averages
            stats.setAverageSleepQuality(calculateAverageSleepQuality(allDreams));
            stats.setAverageLucidity(calculateAverageLucidity(allDreams));

            // Frequency analysis
            stats.setEmotionFrequency(analyzeEmotionFrequency(allDreams));
            stats.setSymbolFrequency(analyzeSymbolFrequency(allDreams));
            stats.setDreamsPerDay(analyzeDreamsPerDay(allDreams));

            // Top items
            stats.setMostCommonEmotion(findMostCommonEmotion(stats.getEmotionFrequency()));
            stats.setTopSymbols(findTopSymbols(stats.getSymbolFrequency(), 5));

            // Streaks
            stats.setLongestDreamStreak(calculateLongestStreak(allDreams));
            stats.setCurrentStreak(calculateCurrentStreak(allDreams));

            return stats;

        } catch (Exception e) {
            e.printStackTrace();
            return initializeEmptyStats();
        }
    }

    public List<DreamInsight> generatePersonalizedInsights(int userId) {
        List<DreamInsight> insights = new ArrayList<>();
        try {
            List<DreamEntry> dreams = databaseService.getAllDreams(userId);
            if (dreams.isEmpty()) {
                insights.add(new DreamInsight(
                        "Welcome to Lucidia!",
                        "Start recording your dreams to unlock personalized insights and patterns.",
                        DreamInsight.InsightType.SUGGESTION,
                        "Record your first dream to begin your journey."
                ));
                return insights;
            }

            DreamStatistics stats = generateUserStatistics(userId);

            // Generate insights based on statistics
            insights.addAll(generateConsistencyInsights(stats, dreams));
            insights.addAll(generateEmotionalInsights(stats));
            insights.addAll(generateSleepInsights(stats));
            insights.addAll(generateSymbolInsights(stats));
            insights.addAll(generateAchievementInsights(stats));

            return insights;

        } catch (Exception e) {
            e.printStackTrace();
            return insights;
        }
    }

    // ========== PRIVATE HELPER METHODS ==========

    private DreamStatistics initializeEmptyStats() {
        DreamStatistics stats = new DreamStatistics();
        stats.setTotalDreams(0);
        stats.setDreamsThisMonth(0);
        stats.setDreamsThisWeek(0);
        stats.setAverageSleepQuality(0);
        stats.setAverageLucidity(0);
        stats.setMostCommonEmotion("No data");
        stats.setTopSymbols(new ArrayList<>());
        stats.setDreamsPerDay(new HashMap<>());
        stats.setEmotionFrequency(new HashMap<>());
        stats.setSymbolFrequency(new HashMap<>());
        stats.setLongestDreamStreak(0);
        stats.setCurrentStreak(0);
        return stats;
    }

    private int countDreamsThisMonth(List<DreamEntry> dreams) {
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        return (int) dreams.stream()
                .filter(dream -> !dream.getDreamDate().isBefore(firstOfMonth))
                .count();
    }

    private int countDreamsThisWeek(List<DreamEntry> dreams) {
        LocalDate startOfWeek = LocalDate.now().minusDays(7);
        return (int) dreams.stream()
                .filter(dream -> !dream.getDreamDate().isBefore(startOfWeek))
                .count();
    }

    private double calculateAverageSleepQuality(List<DreamEntry> dreams) {
        return dreams.stream()
                .mapToInt(DreamEntry::getSleepQuality)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageLucidity(List<DreamEntry> dreams) {
        return dreams.stream()
                .mapToInt(DreamEntry::getLucidityLevel)
                .average()
                .orElse(0.0);
    }

    private Map<String, Integer> analyzeEmotionFrequency(List<DreamEntry> dreams) {
        Map<String, Integer> emotionCount = new HashMap<>();

        for (DreamEntry dream : dreams) {
            Map<String, Double> emotions = nlpService.analyzeEmotion(dream.getDreamText());
            String dominantEmotion = emotions.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("neutral");

            emotionCount.put(dominantEmotion, emotionCount.getOrDefault(dominantEmotion, 0) + 1);
        }

        return emotionCount;
    }

    private Map<String, Integer> analyzeSymbolFrequency(List<DreamEntry> dreams) {
        Map<String, Integer> symbolCount = new HashMap<>();

        for (DreamEntry dream : dreams) {
            List<String> symbols = nlpService.extractSymbols(dream.getDreamText());
            for (String symbol : symbols) {
                symbolCount.put(symbol, symbolCount.getOrDefault(symbol, 0) + 1);
            }
        }

        return symbolCount;
    }

    private Map<LocalDate, Integer> analyzeDreamsPerDay(List<DreamEntry> dreams) {
        return dreams.stream()
                .collect(Collectors.groupingBy(
                        DreamEntry::getDreamDate,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private String findMostCommonEmotion(Map<String, Integer> emotionFrequency) {
        return emotionFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No data");
    }

    private List<String> findTopSymbols(Map<String, Integer> symbolFrequency, int limit) {
        return symbolFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private int calculateLongestStreak(List<DreamEntry> dreams) {
        List<LocalDate> dreamDates = dreams.stream()
                .map(DreamEntry::getDreamDate)
                .sorted()
                .collect(Collectors.toList());

        if (dreamDates.isEmpty()) return 0;

        int longestStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < dreamDates.size(); i++) {
            long daysBetween = ChronoUnit.DAYS.between(dreamDates.get(i - 1), dreamDates.get(i));
            if (daysBetween == 1) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return longestStreak;
    }

    private int calculateCurrentStreak(List<DreamEntry> dreams) {
        List<LocalDate> dreamDates = dreams.stream()
                .map(DreamEntry::getDreamDate)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        if (dreamDates.isEmpty()) return 0;

        int currentStreak = 0;
        LocalDate currentDate = LocalDate.now();

        for (LocalDate dreamDate : dreamDates) {
            long daysBetween = ChronoUnit.DAYS.between(dreamDate, currentDate);
            if (daysBetween == currentStreak) {
                currentStreak++;
            } else {
                break;
            }
        }

        return currentStreak;
    }

    // ========== INSIGHT GENERATION METHODS ==========

    private List<DreamInsight> generateConsistencyInsights(DreamStatistics stats, List<DreamEntry> dreams) {
        List<DreamInsight> insights = new ArrayList<>();

        if (stats.getCurrentStreak() >= 7) {
            insights.add(new DreamInsight(
                    "Consistent Dreamer!",
                    "You've recorded dreams for " + stats.getCurrentStreak() + " consecutive days.",
                    DreamInsight.InsightType.ACHIEVEMENT,
                    "Keep up the great work! Consistency improves dream recall."
            ));
        }

        if (stats.getDreamsThisWeek() < 3 && dreams.size() >= 5) {
            insights.add(new DreamInsight(
                    "Improve Recall Frequency",
                    "You're remembering fewer dreams this week compared to your average.",
                    DreamInsight.InsightType.SUGGESTION,
                    "Try setting a consistent bedtime and keeping a journal by your bed."
            ));
        }

        return insights;
    }

    private List<DreamInsight> generateEmotionalInsights(DreamStatistics stats) {
        List<DreamInsight> insights = new ArrayList<>();

        String dominantEmotion = stats.getMostCommonEmotion();
        if (!"No data".equals(dominantEmotion)) {
            insights.add(new DreamInsight(
                    "Emotional Pattern Detected",
                    "Your dreams are most frequently " + dominantEmotion + ".",
                    DreamInsight.InsightType.PATTERN,
                    getEmotionRecommendation(dominantEmotion)
            ));
        }

        return insights;
    }

    private List<DreamInsight> generateSleepInsights(DreamStatistics stats) {
        List<DreamInsight> insights = new ArrayList<>();

        if (stats.getAverageSleepQuality() < 6) {
            insights.add(new DreamInsight(
                    "Sleep Quality Alert",
                    "Your average sleep quality is " + String.format("%.1f", stats.getAverageSleepQuality()) + "/10.",
                    DreamInsight.InsightType.SUGGESTION,
                    "Consider improving sleep hygiene: reduce screen time before bed, maintain a cool room temperature."
            ));
        }

        if (stats.getAverageLucidity() > 3) {
            insights.add(new DreamInsight(
                    "Lucid Dreaming Potential",
                    "You're experiencing good lucidity levels in your dreams.",
                    DreamInsight.InsightType.ACHIEVEMENT,
                    "Practice reality checks during the day to enhance lucid dreaming."
            ));
        }

        return insights;
    }

    private List<DreamInsight> generateSymbolInsights(DreamStatistics stats) {
        List<DreamInsight> insights = new ArrayList<>();

        if (!stats.getTopSymbols().isEmpty()) {
            insights.add(new DreamInsight(
                    "Recurring Symbols",
                    "Your most common dream symbols: " + String.join(", ", stats.getTopSymbols()),
                    DreamInsight.InsightType.PATTERN,
                    "These symbols may represent important themes in your subconscious mind."
            ));
        }

        return insights;
    }

    private List<DreamInsight> generateAchievementInsights(DreamStatistics stats) {
        List<DreamInsight> insights = new ArrayList<>();

        if (stats.getTotalDreams() >= 10) {
            insights.add(new DreamInsight(
                    "Dream Explorer",
                    "You've recorded " + stats.getTotalDreams() + " dreams!",
                    DreamInsight.InsightType.ACHIEVEMENT,
                    "You're building a valuable record of your dream journey."
            ));
        }

        if (stats.getLongestDreamStreak() >= 14) {
            insights.add(new DreamInsight(
                    "Dedicated Journaler",
                    "Your longest recording streak is " + stats.getLongestDreamStreak() + " days.",
                    DreamInsight.InsightType.ACHIEVEMENT,
                    "This consistency is excellent for pattern recognition."
            ));
        }

        return insights;
    }

    private String getEmotionRecommendation(String emotion) {
        switch (emotion.toLowerCase()) {
            case "fear":
                return "Consider relaxation techniques before bed to reduce anxiety-driven dreams.";
            case "joy":
                return "Your positive dream emotions may reflect good mental well-being.";
            case "anxiety":
                return "Practice stress-reduction techniques during the day.";
            case "sadness":
                return "These dreams may be processing emotional healing.";
            default:
                return "Reflect on how these emotional patterns relate to your waking life.";
        }
    }
}