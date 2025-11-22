package com.lucidia.lucidia.service;

import com.lucidia.lucidia.model.AnalysisResult;
import com.lucidia.lucidia.model.DreamEntry;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DreamAnalysisService {
    private final DatabaseService databaseService;
    private final NLPService nlpService;

    // Dream symbol meanings based on common psychological interpretations
    private static final Map<String, String> SYMBOL_MEANINGS = Map.of(
            "water", "Emotions, subconscious, purification, or life changes",
            "flying", "Freedom, ambition, desire to escape limitations",
            "falling", "Loss of control, insecurity, or fear of failure",
            "death", "Transformation, end of a phase, or major life change",
            "house", "The self, mind, or different aspects of personality",
            "car", "Life direction, control over your path",
            "snake", "Transformation, healing, or hidden fears",
            "fire", "Passion, destruction, purification, or anger",
            "baby", "New beginnings, potential, or vulnerability",
            "school", "Learning experiences, judgment, or past anxieties"
    );

    public DreamAnalysisService() {
        this.databaseService = DatabaseService.getInstance();
        this.nlpService = new NLPService();
    }

    public AnalysisResult performFullAnalysis(DreamEntry dream) {
        try {
            // First, save the dream entry to get a valid ID
            int dreamId;
            if (dream.getId() <= 0) {
                dreamId = databaseService.saveDreamEntry(dream);
                dream.setId(dreamId);
            } else {
                dreamId = dream.getId();
                if (!databaseService.dreamExists(dreamId)) {
                    dreamId = databaseService.saveDreamEntry(dream);
                    dream.setId(dreamId);
                }
            }

            // Perform NLP analysis using compatible methods
            Map<String, Double> emotions = nlpService.analyzeEmotion(dream.getDreamText());
            List<String> symbols = nlpService.extractSymbols(dream.getDreamText());

            // Generate detailed interpretation using compatible method
            String interpretation = generateDetailedInterpretation(
                    dream.getDreamText(), emotions, symbols, dream.getLucidityLevel()
            );

            // Create analysis result
            AnalysisResult result = new AnalysisResult(emotions, symbols, interpretation);
            result.setDreamId(dreamId);

            // Save analysis to database
            saveAnalysisResult(result);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to perform dream analysis", e);
        }
    }

    // Alternative method that saves the dream first, then analyzes
    public AnalysisResult performFullAnalysis(String dreamText, java.time.LocalDate dreamDate, int sleepQuality, int lucidityLevel) {
        try {
            // Create and save dream entry first
            DreamEntry dream = new DreamEntry(1, dreamText, dreamDate, sleepQuality, lucidityLevel);
            int dreamId = databaseService.saveDreamEntry(dream);
            dream.setId(dreamId);

            // Now perform analysis
            return performFullAnalysis(dream);

        } catch (Exception e) {
            throw new RuntimeException("Failed to perform dream analysis", e);
        }
    }

    private String generateDetailedInterpretation(String dreamText, Map<String, Double> emotions,
                                                  List<String> symbols, int lucidityLevel) {
        StringBuilder interpretation = new StringBuilder();

        // Emotional Analysis Section
        interpretation.append("🧠 EMOTIONAL ANALYSIS\n");
        interpretation.append("=" .repeat(40)).append("\n");

        String dominantEmotion = emotions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");

        interpretation.append("Primary Emotion: ").append(capitalizeFirst(dominantEmotion)).append("\n");
        interpretation.append("Emotional Intensity: ");

        double intensity = emotions.getOrDefault(dominantEmotion, 0.0);
        if (intensity > 0.7) interpretation.append("High");
        else if (intensity > 0.4) interpretation.append("Moderate");
        else interpretation.append("Low");

        interpretation.append("\n\nDetailed Emotional Profile:\n");
        emotions.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> interpretation.append("• ")
                        .append(capitalizeFirst(entry.getKey()))
                        .append(": ").append(String.format("%.0f%%", entry.getValue() * 100))
                        .append("\n"));

        // Symbol Analysis Section
        if (!symbols.isEmpty()) {
            interpretation.append("\n🔮 SYMBOLIC ANALYSIS\n");
            interpretation.append("=" .repeat(40)).append("\n");
            interpretation.append("Key Symbols Found: ").append(symbols.size()).append("\n\n");

            for (String symbol : symbols) {
                interpretation.append("🔸 ").append(capitalizeFirst(symbol)).append("\n");
                interpretation.append("   Meaning: ").append(SYMBOL_MEANINGS.getOrDefault(symbol, "Personal significance may vary")).append("\n\n");
            }
        }

        // Lucidity Assessment
        interpretation.append("🌙 LUCIDITY ASSESSMENT\n");
        interpretation.append("=" .repeat(40)).append("\n");
        interpretation.append("Lucidity Level: ").append(lucidityLevel).append("/5\n");

        if (lucidityLevel >= 3) {
            interpretation.append("High lucidity - You had good awareness and control in this dream.\n");
        } else if (lucidityLevel >= 1) {
            interpretation.append("Partial lucidity - Some awareness of the dream state was present.\n");
        } else {
            interpretation.append("Non-lucid dream - Full immersion in the dream narrative.\n");
        }

        // Psychological Interpretation
        interpretation.append("\n💭 PSYCHOLOGICAL INTERPRETATION\n");
        interpretation.append("=" .repeat(40)).append("\n");
        interpretation.append(generatePsychologicalInsight(dominantEmotion, symbols, dreamText));

        // Recommendations
        interpretation.append("\n✨ RECOMMENDATIONS\n");
        interpretation.append("=" .repeat(40)).append("\n");
        interpretation.append(generateRecommendations(dominantEmotion, symbols, lucidityLevel));

        return interpretation.toString();
    }

    private String generatePsychologicalInsight(String dominantEmotion, List<String> symbols, String dreamText) {
        StringBuilder insight = new StringBuilder();

        switch (dominantEmotion) {
            case "fear" -> {
                insight.append("This dream may reflect underlying anxieties or stressors in your waking life. ");
                if (symbols.contains("falling")) {
                    insight.append("The falling sensation suggests feelings of losing control or fear of failure. ");
                }
                if (symbols.contains("dark")) {
                    insight.append("Darkness often represents the unknown or repressed aspects of the psyche. ");
                }
            }
            case "joy" -> {
                insight.append("This dream indicates positive emotional processing and mental well-being. ");
                if (symbols.contains("flying")) {
                    insight.append("Flying represents freedom and the desire to transcend current limitations. ");
                }
                if (symbols.contains("light")) {
                    insight.append("Light symbolizes consciousness, clarity, and spiritual awareness. ");
                }
            }
            case "anxiety" -> {
                insight.append("This dream may be processing daily stresses or unresolved concerns. ");
                if (symbols.contains("chase")) {
                    insight.append("Being chased often reflects avoidance of confronting difficult issues. ");
                }
            }
            case "sadness" -> {
                insight.append("This dream may be helping you process grief, loss, or emotional healing. ");
                if (symbols.contains("death")) {
                    insight.append("Death in dreams typically symbolizes transformation rather than literal death. ");
                }
            }
            default -> insight.append("This dream shows balanced emotional processing with multiple themes present. ");
        }

        return insight.toString();
    }

    private String generateRecommendations(String dominantEmotion, List<String> symbols, int lucidityLevel) {
        StringBuilder recommendations = new StringBuilder();

        // Emotion-based recommendations
        switch (dominantEmotion) {
            case "fear", "anxiety" -> {
                recommendations.append("• Practice relaxation techniques before sleep\n");
                recommendations.append("• Consider journaling about current stressors\n");
                recommendations.append("• Try progressive muscle relaxation\n");
            }
            case "sadness" -> {
                recommendations.append("• Allow yourself time to process emotions\n");
                recommendations.append("• Consider talking to someone about your feelings\n");
                recommendations.append("• Practice self-compassion\n");
            }
            case "joy" -> {
                recommendations.append("• Reflect on what brings you happiness\n");
                recommendations.append("• Try to incorporate more positive activities into daily life\n");
            }
        }

        // Lucidity recommendations
        if (lucidityLevel < 2) {
            recommendations.append("• Practice reality checks during the day\n");
            recommendations.append("• Keep a consistent dream journal\n");
            recommendations.append("• Try the MILD (Mnemonic Induction) technique\n");
        } else {
            recommendations.append("• Continue practicing lucid dreaming techniques\n");
            recommendations.append("• Experiment with dream control exercises\n");
        }

        return recommendations.toString();
    }

    private void saveAnalysisResult(AnalysisResult result) throws SQLException {
        // First verify the dream exists
        if (!databaseService.dreamExists(result.getDreamId())) {
            throw new SQLException("Cannot save analysis: Dream with ID " + result.getDreamId() + " does not exist");
        }

        String sql = "INSERT INTO dream_analysis (dream_id, emotion_score, dominant_emotion, symbols_detected, interpretation_text, confidence_score) VALUES (?, ?, ?, ?, ?, ?)";

        Connection connection = databaseService.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, result.getDreamId());
            stmt.setDouble(2, result.getEmotionScore());
            stmt.setString(3, result.getDominantEmotion());

            // Handle symbols list
            String symbolsStr = "";
            if (result.getSymbolsDetected() != null && !result.getSymbolsDetected().isEmpty()) {
                symbolsStr = String.join(",", result.getSymbolsDetected());
            }
            stmt.setString(4, symbolsStr);

            stmt.setString(5, result.getInterpretationText());
            stmt.setDouble(6, result.getConfidenceScore());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating analysis failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    result.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<AnalysisResult> getUserAnalysisHistory(int userId) throws SQLException {
        List<AnalysisResult> results = new ArrayList<>();
        String sql = """
            SELECT da.*, de.user_id 
            FROM dream_analysis da 
            JOIN dream_entries de ON da.dream_id = de.id 
            WHERE de.user_id = ? 
            ORDER BY da.analysis_timestamp DESC
            """;

        Connection connection = databaseService.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AnalysisResult result = new AnalysisResult();
                result.setId(rs.getInt("id"));
                result.setDreamId(rs.getInt("dream_id"));
                result.setEmotionScore(rs.getDouble("emotion_score"));
                result.setDominantEmotion(rs.getString("dominant_emotion"));

                String symbolsStr = rs.getString("symbols_detected");
                if (symbolsStr != null && !symbolsStr.isEmpty()) {
                    result.setSymbolsDetected(Arrays.asList(symbolsStr.split(",")));
                }

                result.setInterpretationText(rs.getString("interpretation_text"));
                result.setConfidenceScore(rs.getDouble("confidence_score"));
                results.add(result);
            }
        }

        return results;
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}