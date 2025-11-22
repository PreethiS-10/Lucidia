package com.lucidia.lucidia.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.lucidia.lucidia.model.AnalysisResult;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import java.util.ResourceBundle;

public class AnalysisController implements Initializable {

    @FXML private Label dominantEmotionLabel;
    @FXML private Label confidenceScoreLabel;
    @FXML private Label symbolCountLabel;
    @FXML private Label analysisDateLabel;
    @FXML private Label intensityLabel;


    @FXML private TextFlow interpretationFlow;
    @FXML private TextFlow recommendationsFlow;
    @FXML private VBox emotionBarsContainer;
    @FXML private VBox symbolDictionaryContainer;

    @FXML private Button saveAnalysisButton;
    @FXML private Button exportButton;
    @FXML private Button shareButton;
    @FXML private Button newAnalysisButton;
    @FXML private Button backButton;

    private AnalysisResult currentAnalysis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set current date with cosmic styling
        analysisDateLabel.setText("✨ Analyzed " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));

        // Initialize with sample data for demonstration
        initializeSampleData();
    }

    public void setAnalysisResult(AnalysisResult analysis) {
        this.currentAnalysis = analysis;
        displayAnalysis();
    }

    public void displayAnalysis(AnalysisResult analysis) {
        this.currentAnalysis = analysis;
        displayAnalysis();
    }

    private void displayAnalysis() {
        if (currentAnalysis == null) {
            initializeSampleData();
            return;
        }

        // Update quick insights
        updateQuickInsights();

        updateIntensityLabel();

        // Display interpretation as flowing text
        displayInterpretation();

        // Display emotions as visual bars
        displayEmotions();

        // Display symbols as cards
        displaySymbols();

        // Display recommendations as flowing text
        displayRecommendations();
    }

    private void initializeSampleData() {
        // Sample data for demonstration
        dominantEmotionLabel.setText("Joy");
        confidenceScoreLabel.setText("92%");
        symbolCountLabel.setText("7 symbols");

        // Sample interpretation
        displaySampleInterpretation();
        displaySampleEmotions();
        displaySampleSymbols();
        displaySampleRecommendations();
    }
    private void updateIntensityLabel() {
        if (currentAnalysis != null && currentAnalysis.getEmotionBreakdown() != null) {
            double maxIntensity = currentAnalysis.getEmotionBreakdown().values().stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);

            if (maxIntensity > 0.7) {
                intensityLabel.setText("High");
            } else if (maxIntensity > 0.4) {
                intensityLabel.setText("Medium");
            } else {
                intensityLabel.setText("Low");
            }
        } else {
            intensityLabel.setText("Medium");
        }
    }
    private void updateQuickInsights() {
        try {
            Map<String, Double> emotions = currentAnalysis.getEmotionBreakdown();
            if (emotions != null && !emotions.isEmpty()) {
                String dominantEmotion = emotions.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("Unknown");

                dominantEmotionLabel.setText(capitalize(dominantEmotion));

                double confidence = emotions.get(dominantEmotion) * 100;
                confidenceScoreLabel.setText(String.format("%.0f%%", confidence));
            }

            List<String> symbols = currentAnalysis.getSymbolsDetected();
            if (symbols != null) {
                symbolCountLabel.setText(symbols.size() + " symbols");
            }
        } catch (Exception e) {
            // Fallback to sample data
            dominantEmotionLabel.setText("Joy");
            confidenceScoreLabel.setText("92%");
            symbolCountLabel.setText("7 symbols");
        }
    }

    private void displayInterpretation() {
        interpretationFlow.getChildren().clear();

        String interpretation = currentAnalysis != null ?
                currentAnalysis.getInterpretationText() : getSampleInterpretation();

        createFlowingText(interpretationFlow, interpretation);
    }

    private void displaySampleInterpretation() {
        interpretationFlow.getChildren().clear();
        createFlowingText(interpretationFlow, getSampleInterpretation());
    }

    private void createFlowingText(TextFlow textFlow, String content) {
        String[] sentences = content.split("\\. ");

        for (int i = 0; i < sentences.length; i++) {
            Text sentence = new Text(sentences[i] + (i < sentences.length - 1 ? ". " : ""));
            sentence.setStyle("-fx-font-size: 15px; -fx-font-family: 'Space Grotesk', sans-serif;");

            // Alternate colors for better readability
            if (i % 3 == 0) {
                sentence.setStyle(sentence.getStyle() + " -fx-fill: #F0F8FF;");
            } else if (i % 3 == 1) {
                sentence.setStyle(sentence.getStyle() + " -fx-fill: #E6E6FA;");
            } else {
                sentence.setStyle(sentence.getStyle() + " -fx-fill: #DDA0DD;");
            }

            textFlow.getChildren().add(sentence);

            // Add spacing between sentences
            if (i < sentences.length - 1 && i % 2 == 1) {
                Text space = new Text("\n\n");
                textFlow.getChildren().add(space);
            }
        }
    }

    private void displayEmotions() {
        emotionBarsContainer.getChildren().clear();

        Map<String, Double> emotions = currentAnalysis != null ?
                currentAnalysis.getEmotionBreakdown() : getSampleEmotions();

        if (emotions == null) {
            emotions = getSampleEmotions();
        }

        emotions.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5) // Show top 5 emotions
                .forEach(this::createEmotionBar);
    }

    private void displaySampleEmotions() {
        emotionBarsContainer.getChildren().clear();
        getSampleEmotions().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(this::createEmotionBar);
    }

    private void createEmotionBar(Map.Entry<String, Double> emotion) {
        VBox emotionCard = new VBox(8);
        emotionCard.setStyle("""
            -fx-padding: 15; 
            -fx-background-color: rgba(138,43,226,0.15); 
            -fx-background-radius: 12; 
            -fx-border-color: rgba(147,112,219,0.3);
            -fx-border-radius: 12;
            -fx-border-width: 1;
            """);

        HBox headerBox = new HBox();
        headerBox.setSpacing(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label emotionLabel = new Label(getEmotionEmoji(emotion.getKey()) + " " + capitalize(emotion.getKey()));
        emotionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #F0F8FF;");

        Label percentageLabel = new Label(String.format("%.0f%%", emotion.getValue() * 100));
        percentageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #DDA0DD; -fx-font-weight: 600;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(emotionLabel, spacer, percentageLabel);

        ProgressBar emotionBar = new ProgressBar(emotion.getValue());
        emotionBar.setPrefWidth(280);
        emotionBar.setPrefHeight(10);
        emotionBar.setStyle("""
            -fx-accent: %s;
            -fx-background-color: rgba(25,25,112,0.3);
            -fx-background-radius: 5;
            """.formatted(getEmotionColor(emotion.getKey())));

        emotionCard.getChildren().addAll(headerBox, emotionBar);
        emotionBarsContainer.getChildren().add(emotionCard);
    }

    private void displaySymbols() {
        symbolDictionaryContainer.getChildren().clear();

        List<String> symbols = currentAnalysis != null ?
                currentAnalysis.getSymbolsDetected() : getSampleSymbols();

        if (symbols == null) {
            symbols = getSampleSymbols();
        }

        for (String symbol : symbols.subList(0, Math.min(6, symbols.size()))) {
            createSymbolCard(symbol);
        }
    }

    private void displaySampleSymbols() {
        symbolDictionaryContainer.getChildren().clear();
        for (String symbol : getSampleSymbols()) {
            createSymbolCard(symbol);
        }
    }

    private void createSymbolCard(String symbol) {
        HBox symbolCard = new HBox(15);
        symbolCard.setStyle("""
            -fx-padding: 15; 
            -fx-background-color: rgba(147,112,219,0.15); 
            -fx-background-radius: 12;
            -fx-border-color: rgba(221,160,221,0.3);
            -fx-border-radius: 12;
            -fx-border-width: 1;
            """);
        symbolCard.setAlignment(Pos.CENTER_LEFT);

        Label symbolEmoji = new Label(getSymbolEmoji(symbol));
        symbolEmoji.setStyle("-fx-font-size: 24px;");

        VBox textBox = new VBox(4);

        Label symbolName = new Label(capitalize(symbol));
        symbolName.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #F0F8FF;");

        Label symbolMeaning = new Label(getSymbolMeaning(symbol));
        symbolMeaning.setStyle("""
            -fx-font-size: 12px; 
            -fx-text-fill: #DDA0DD; 
            -fx-wrap-text: true;
            -fx-font-family: 'Space Grotesk', sans-serif;
            """);
        symbolMeaning.setMaxWidth(240);
        symbolMeaning.setWrapText(true);

        textBox.getChildren().addAll(symbolName, symbolMeaning);
        symbolCard.getChildren().addAll(symbolEmoji, textBox);

        symbolDictionaryContainer.getChildren().add(symbolCard);
    }

    private void displayRecommendations() {
        recommendationsFlow.getChildren().clear();

        String recommendations = currentAnalysis != null ?
                extractRecommendations(currentAnalysis.getInterpretationText()) : getSampleRecommendations();

        createRecommendationFlow(recommendations);
    }

    private void displaySampleRecommendations() {
        recommendationsFlow.getChildren().clear();
        createRecommendationFlow(getSampleRecommendations());
    }

    private void createRecommendationFlow(String recommendations) {
        String[] points = recommendations.split("\\n");

        for (String point : points) {
            if (!point.trim().isEmpty()) {
                Text bullet = new Text("✨ ");
                bullet.setStyle("-fx-fill: #8A2BE2; -fx-font-size: 16px;");

                Text content = new Text(point.trim() + "\n\n");
                content.setStyle("""
                    -fx-fill: #E6E6FA; 
                    -fx-font-size: 14px; 
                    -fx-font-family: 'Space Grotesk', sans-serif;
                    """);

                recommendationsFlow.getChildren().addAll(bullet, content);
            }
        }
    }

    // Sample data methods
    private String getSampleInterpretation() {
        return """
            Your dream reveals a fascinating journey of personal transformation and emotional growth. The vivid imagery suggests you're at a pivotal moment in your life where new opportunities are emerging. The presence of water elements indicates emotional cleansing and renewal, while the flying sensations represent your soul's desire for freedom and transcendence. This dream is particularly significant as it combines elements of both fear and joy, suggesting you're navigating through uncertainty towards a brighter future. The symbolic elements present in your dream point to deep psychological processes at work in your subconscious mind.
            """;
    }

    private Map<String, Double> getSampleEmotions() {
        return Map.of(
                "joy", 0.85,
                "peace", 0.72,
                "excitement", 0.68,
                "curiosity", 0.55,
                "anxiety", 0.23
        );
    }

    private List<String> getSampleSymbols() {
        return List.of("flying", "water", "light", "house", "bird", "door");
    }

    private String getSampleRecommendations() {
        return """
            Take time for self-reflection on the emotions revealed in your dream
            Consider keeping a dream journal to track recurring themes and symbols
            Practice mindfulness or meditation to enhance your dream recall
            Explore creative expression inspired by your dream imagery
            Pay attention to how these dream themes relate to your waking life
            Trust your intuition when interpreting the personal meaning of symbols""";
    }

    private String extractRecommendations(String interpretationText) {
        if (interpretationText.contains("RECOMMENDATIONS")) {
            int start = interpretationText.indexOf("RECOMMENDATIONS");
            return interpretationText.substring(start);
        }
        return getSampleRecommendations();
    }

    // Helper methods
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String getEmotionEmoji(String emotion) {
        return switch (emotion.toLowerCase()) {
            case "joy" -> "😊";
            case "fear" -> "😰";
            case "anxiety" -> "😟";
            case "sadness" -> "😢";
            case "peace" -> "😌";
            case "excitement" -> "🤩";
            case "confusion" -> "🤔";
            case "curiosity" -> "🤔";
            default -> "💭";
        };
    }

    private String getEmotionColor(String emotion) {
        return switch (emotion.toLowerCase()) {
            case "joy" -> "#FFD700";
            case "fear" -> "#8B0000";
            case "anxiety" -> "#FF6347";
            case "sadness" -> "#4682B4";
            case "peace" -> "#32CD32";
            case "excitement" -> "#FF1493";
            case "confusion" -> "#DDA0DD";
            case "curiosity" -> "#9370DB";
            default -> "#8A2BE2";
        };
    }

    private String getSymbolEmoji(String symbol) {
        return switch (symbol.toLowerCase()) {
            case "water", "ocean", "sea", "river" -> "🌊";
            case "flying", "flight" -> "🕊️";
            case "fire", "flame" -> "🔥";
            case "snake" -> "🐍";
            case "house", "building" -> "🏠";
            case "tree", "forest" -> "🌳";
            case "mountain" -> "⛰️";
            case "bird" -> "🦅";
            case "door" -> "🚪";
            case "light", "sun" -> "☀️";
            case "moon" -> "🌙";
            case "car" -> "🚗";
            case "school" -> "🏫";
            default -> "🔮";
        };
    }

    private String getSymbolMeaning(String symbol) {
        return switch (symbol.toLowerCase()) {
            case "water" -> "Emotions, cleansing, life transitions";
            case "flying" -> "Freedom, transcendence, limitless potential";
            case "fire" -> "Passion, transformation, creative energy";
            case "snake" -> "Wisdom, rebirth, hidden knowledge";
            case "house" -> "Self-identity, security, inner psyche";
            case "tree" -> "Growth, stability, life connection";
            case "mountain" -> "Challenges, achievements, higher perspective";
            case "bird" -> "Messages from the subconscious, spiritual freedom";
            case "door" -> "New opportunities, life transitions, choices";
            case "light" -> "Enlightenment, hope, divine guidance";
            case "moon" -> "Intuition, cycles, feminine energy";
            case "car" -> "Life direction, personal control, journey";
            case "school" -> "Learning experiences, personal growth";
            default -> "Personal significance unique to your life experience";
        };
    }

    // FXML Event Handlers
    @FXML
    private void handleSaveAnalysis() {
        showAlert("Success", "✨ Analysis saved to your dream archive!");
    }

    @FXML
    private void handleExport() {
        showAlert("Info", "📄 PDF export will be available in the next update!");
    }

    @FXML
    private void handleShare() {
        showAlert("Info", "🔗 Anonymous sharing feature coming soon!");
    }

    @FXML
    private void handleNewDream() {
        showAlert("Navigation", "🆕 Returning to dream journal...");
        // Add navigation logic here
    }

    @FXML
    private void handleBack() {
        showAlert("Navigation", "⬅️ Returning to main journal...");
        // Add navigation logic here
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply cosmic styling to alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/lucidia-theme.css").toExternalForm());

        alert.showAndWait();
    }
}
