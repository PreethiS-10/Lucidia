package com.lucidia.lucidia.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.lucidia.lucidia.service.DreamVisualizationService;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

public class DreamJournalController implements Initializable {

    // Existing FXML fields
    @FXML private DatePicker dreamDate;
    @FXML private Slider sleepQualitySlider;
    @FXML private Slider luciditySlider;
    @FXML private TextArea dreamContent;
    @FXML private Button saveButton;
    @FXML private Button analyzeButton;
    @FXML private Button voiceButton;
    @FXML private Button clearButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator analysisProgress;

    // New FXML fields for image generation
    @FXML private Button generateImageButton;
    @FXML private Button cancelGenerationButton;
    @FXML private ProgressBar imageGenerationProgress;
    @FXML private Label imageGenerationStatus;
    @FXML private ImageView dreamVisualizationView;

    // Service management
    private DreamVisualizationService currentService;

    // Emotion and symbol detection patterns
    private static final Map<String, Pattern> EMOTION_PATTERNS = createEmotionPatterns();
    private static final Set<String> DREAM_SYMBOLS = createSymbolSet();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dreamDate.setValue(LocalDate.now());

        // Add value change listeners
        sleepQualitySlider.valueProperty().addListener((obs, oldVal, newVal) ->
                updateStatus("Sleep Quality: " + newVal.intValue() + "/10"));

        luciditySlider.valueProperty().addListener((obs, oldVal, newVal) ->
                updateStatus("Lucidity Level: " + newVal.intValue() + "/5"));

        // Initialize image generation UI
        imageGenerationProgress.setVisible(false);
        cancelGenerationButton.setVisible(false);
        imageGenerationStatus.setText("Ready");
    }

    @FXML
    private void handleSave() {
        // Implementation will be handled by MainController
        updateStatus("Dream saved successfully!");
    }

    @FXML
    private void handleAnalyze() {
        analysisProgress.setVisible(true);
        updateStatus("Analyzing dream...");

        // Simulate analysis delay
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    analysisProgress.setVisible(false);
                    updateStatus("Analysis complete!");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @FXML
    private void handleVoiceInput() {
        updateStatus("Voice input feature coming soon...");
    }

    @FXML
    private void handleClear() {
        dreamContent.clear();
        dreamDate.setValue(LocalDate.now());
        sleepQualitySlider.setValue(5);
        luciditySlider.setValue(0);

        // Clear generated image
        dreamVisualizationView.setImage(null);
        imageGenerationStatus.setText("Ready");
        imageGenerationProgress.setProgress(0);

        updateStatus("Form cleared.");
    }

    @FXML
    private void handleGenerateImage() {
        if (dreamContent.getText().trim().isEmpty()) {
            updateStatus("Please enter a dream description first.");
            return;
        }

        // Cancel existing service if running
        if (currentService != null && currentService.isRunning()) {
            currentService.cancel();
        }

        // Create and configure service
        currentService = new DreamVisualizationService();
        currentService.setParameters(
                getDreamText(),
                extractEmotions(),
                extractSymbols()
        );

        // Setup UI bindings
        imageGenerationProgress.setVisible(true);
        cancelGenerationButton.setVisible(true);
        imageGenerationProgress.progressProperty().bind(currentService.progressProperty());
        imageGenerationStatus.textProperty().bind(currentService.messageProperty());
        generateImageButton.disableProperty().bind(currentService.runningProperty());

        // Handle completion events
        currentService.setOnSucceeded(e -> {
            Image generatedImage = currentService.getValue();
            if (generatedImage != null) {
                dreamVisualizationView.setImage(generatedImage);
                dreamVisualizationView.setFitWidth(400);
                dreamVisualizationView.setPreserveRatio(true);
                dreamVisualizationView.setSmooth(true);
                updateStatus("Dream visualization generated successfully!");
            } else {
                updateStatus("Failed to load generated image.");
            }
            resetService();
        });

        currentService.setOnFailed(e -> {
            Throwable exception = currentService.getException();
            String errorMsg = exception.getMessage();
            updateStatus("Generation failed: " + (errorMsg != null ? errorMsg : "Unknown error"));
            System.err.println("Generation error: " + exception);
            resetService();
        });

        currentService.setOnCancelled(e -> {
            updateStatus("Generation cancelled.");
            resetService();
        });

        // Start generation
        currentService.start();
        updateStatus("Starting dream visualization...");
    }

    @FXML
    private void handleCancelGeneration() {
        if (currentService != null && currentService.isRunning()) {
            currentService.cancel();
            updateStatus("Cancelling generation...");
        }
    }

    private void resetService() {
        if (currentService != null) {
            // Unbind properties
            imageGenerationProgress.progressProperty().unbind();
            imageGenerationStatus.textProperty().unbind();
            generateImageButton.disableProperty().unbind();

            currentService = null;
        }

        // Reset UI state
        imageGenerationProgress.setProgress(0);
        imageGenerationProgress.setVisible(false);
        cancelGenerationButton.setVisible(false);
        imageGenerationStatus.setText("Ready");
        generateImageButton.setDisable(false);
    }

    /**
     * Extract emotions from dream text using pattern matching
     */
    private Map<String, Double> extractEmotions() {
        String text = dreamContent.getText().toLowerCase();
        Map<String, Double> emotions = new HashMap<>();

        // Initialize emotion scores
        emotions.put("joy", 0.0);
        emotions.put("fear", 0.0);
        emotions.put("anxiety", 0.0);
        emotions.put("sadness", 0.0);
        emotions.put("peace", 0.0);
        emotions.put("excitement", 0.0);
        emotions.put("confusion", 0.0);

        // Analyze text for emotional indicators [web:51][web:45]
        for (Map.Entry<String, Pattern> entry : EMOTION_PATTERNS.entrySet()) {
            String emotion = entry.getKey();
            Pattern pattern = entry.getValue();

            long matches = pattern.matcher(text).results().count();
            if (matches > 0) {
                double score = Math.min(1.0, matches * 0.2); // Cap at 1.0
                emotions.put(emotion, score);
            }
        }

        return emotions;
    }

    /**
     * Extract dream symbols from text using keyword matching
     */
    private List<String> extractSymbols() {
        String text = dreamContent.getText().toLowerCase();
        List<String> foundSymbols = new ArrayList<>();

        for (String symbol : DREAM_SYMBOLS) {
            if (text.contains(symbol)) {
                foundSymbols.add(symbol);
            }
        }

        // Limit to most relevant symbols
        return foundSymbols.size() > 5 ? foundSymbols.subList(0, 5) : foundSymbols;
    }

    /**
     * Create emotion detection patterns
     */
    private static Map<String, Pattern> createEmotionPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();

        patterns.put("joy", Pattern.compile("\\b(happy|joy|delight|excited|wonderful|amazing|beautiful|love|smiled|laughing|celebration)\\b"));
        patterns.put("fear", Pattern.compile("\\b(scared|afraid|terrified|frightening|horror|panic|nightmare|monster|danger|threat)\\b"));
        patterns.put("anxiety", Pattern.compile("\\b(worried|anxious|nervous|stress|tension|overwhelmed|confused|lost|trapped|hurried)\\b"));
        patterns.put("sadness", Pattern.compile("\\b(sad|crying|tears|lonely|empty|dark|gloomy|depressed|grief|loss)\\b"));
        patterns.put("peace", Pattern.compile("\\b(calm|peaceful|serene|quiet|gentle|soft|warm|comfort|safe|relaxed)\\b"));
        patterns.put("excitement", Pattern.compile("\\b(thrilled|energetic|adventure|flying|fast|rushing|bright|intense|powerful)\\b"));
        patterns.put("confusion", Pattern.compile("\\b(confused|strange|weird|bizarre|unclear|foggy|mixed|chaotic|disoriented)\\b"));

        return patterns;
    }

    /**
     * Create common dream symbol set
     */
    private static Set<String> createSymbolSet() {
        return Set.of(
                "water", "ocean", "sea", "river", "rain", "flood",
                "flying", "falling", "running", "chasing", "escape",
                "fire", "flame", "burning", "light", "sun", "moon",
                "snake", "spider", "dog", "cat", "bird", "animal",
                "house", "building", "door", "window", "stairs", "room",
                "car", "road", "bridge", "mountain", "forest", "tree",
                "person", "stranger", "family", "friend", "child",
                "death", "blood", "weapon", "fight", "war",
                "school", "teacher", "test", "book", "work",
                "mirror", "shadow", "mask", "key", "treasure"
        );
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    // Getters for accessing form data
    public String getDreamText() {
        return dreamContent.getText();
    }

    public LocalDate getDreamDate() {
        return dreamDate.getValue();
    }

    public int getSleepQuality() {
        return (int) sleepQualitySlider.getValue();
    }

    public int getLucidityLevel() {
        return (int) luciditySlider.getValue();
    }
}
