package com.lucidia.lucidia.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.concurrent.Worker;
import javafx.application.Platform;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.lucidia.lucidia.model.DreamEntry;
import com.lucidia.lucidia.model.DreamStatistics;
import com.lucidia.lucidia.service.DatabaseService;
import com.lucidia.lucidia.service.NLPService;
import com.lucidia.lucidia.service.DreamAnalysisService;
import com.lucidia.lucidia.service.DreamAnalyticsService;
import com.lucidia.lucidia.model.AnalysisResult;
import com.lucidia.lucidia.service.DreamVisualizationService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController implements Initializable {

    // Main UI Components
    @FXML private TabPane mainTabPane;
    @FXML private Tab dreamJournalTab;
    @FXML private Tab dreamHistoryTab;
    @FXML private Tab analyticsTab;

    @FXML private DatePicker dreamDatePicker;
    @FXML private Spinner<Integer> sleepQualitySpinner;
    @FXML private Spinner<Integer> luciditySpinner;
    @FXML private TextArea dreamTextArea;
    @FXML private Label wordCountLabel;
    @FXML private Button saveDreamButton;
    @FXML private Button analyzeDreamButton;
    @FXML private Button clearButton;
    @FXML private TextArea analysisResultArea;
    @FXML private ProgressIndicator analysisProgress;
    @FXML private ListView<String> dreamHistoryList;
    @FXML private Button refreshHistoryButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label statusLabel;

    // Dream Visualization Components
    @FXML private ImageView dreamVisualizationView;
    @FXML private Button generateImageButton;
    @FXML private Button cancelImageButton;
    @FXML private ProgressBar imageGenerationProgress;
    @FXML private Label imageGenerationStatus;

    // Analytics Preview Components
    @FXML private Label previewTotalDreams;
    @FXML private Label previewSleepQuality;
    @FXML private Label previewLucidity;
    @FXML private Label previewStreak;

    // Services
    private DatabaseService databaseService;
    private NLPService nlpService;
    private DreamVisualizationService visualizationService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        databaseService = DatabaseService.getInstance();
        nlpService = new NLPService();
        visualizationService = new DreamVisualizationService();

        // Set default date to today
        dreamDatePicker.setValue(LocalDate.now());

        // Initialize spinners
        sleepQualitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5));
        luciditySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));

        // Setup text area auto-resize
        setupTextAreaAutoResize();

        // Setup image view
        if (dreamVisualizationView != null) {
            dreamVisualizationView.setFitWidth(500);
            dreamVisualizationView.setFitHeight(350);
            dreamVisualizationView.setPreserveRatio(true);
            dreamVisualizationView.setSmooth(true);
        }

        // Initialize image generation UI
        if (imageGenerationProgress != null) {
            imageGenerationProgress.setVisible(false);
        }
        if (cancelImageButton != null) {
            cancelImageButton.setVisible(false);
        }
        if (imageGenerationStatus != null) {
            imageGenerationStatus.setText("🌟 Ready to create magic");
        }

        // Initialize dream history
        initializeDreamHistory();

        // Initialize analytics preview
        initializeAnalyticsPreview();

        // Set initial status
        updateStatus("✨ Ready to capture dreams...");
    }

    private void initializeAnalyticsPreview() {
        // Update preview stats immediately
        updateAnalyticsPreview();

        // Set up listener for when analytics tab is selected
        if (analyticsTab != null) {
            analyticsTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    updateAnalyticsPreview();
                }
            });
        }
    }

    private void updateAnalyticsPreview() {
        try {
            DreamAnalyticsService analyticsService = new DreamAnalyticsService();
            DreamStatistics stats = analyticsService.generateUserStatistics(1);

            if (previewTotalDreams != null) {
                previewTotalDreams.setText(String.valueOf(stats.getTotalDreams()));
            }
            if (previewSleepQuality != null) {
                previewSleepQuality.setText(String.format("%.1f", stats.getAverageSleepQuality()));
            }
            if (previewLucidity != null) {
                previewLucidity.setText(String.format("%.1f", stats.getAverageLucidity()));
            }
            if (previewStreak != null) {
                previewStreak.setText(String.valueOf(stats.getCurrentStreak()));
            }
        } catch (Exception e) {
            // Set default values if analytics service fails
            if (previewTotalDreams != null) previewTotalDreams.setText("0");
            if (previewSleepQuality != null) previewSleepQuality.setText("0.0");
            if (previewLucidity != null) previewLucidity.setText("0.0");
            if (previewStreak != null) previewStreak.setText("0");
        }
    }

    private void setupTextAreaAutoResize() {
        // Dream text area auto-resize behavior
        dreamTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateTextAreaSize(dreamTextArea, newValue);
            updateWordCount(newValue);
        });

        // Analysis result area auto-resize behavior
        analysisResultArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateTextAreaSize(analysisResultArea, newValue);
        });

        // Initial sizing
        Platform.runLater(() -> {
            updateTextAreaSize(dreamTextArea, dreamTextArea.getText());
            updateTextAreaSize(analysisResultArea, analysisResultArea.getText());
        });
    }

    private void updateTextAreaSize(TextArea textArea, String text) {
        if (text == null || text.isEmpty()) {
            // Set minimum height when empty
            if (textArea == dreamTextArea) {
                textArea.setPrefHeight(300);
            } else if (textArea == analysisResultArea) {
                textArea.setPrefHeight(200);
            }
            return;
        }

        // Calculate approximate height based on text length and line breaks
        int lineCount = countLines(text);
        double baseHeight;
        double lineHeight;
        double minHeight;
        double maxHeight;

        if (textArea == dreamTextArea) {
            baseHeight = 300;
            lineHeight = 18;
            minHeight = 300;
            maxHeight = 600;
        } else {
            baseHeight = 200;
            lineHeight = 16;
            minHeight = 200;
            maxHeight = 500;
        }

        double calculatedHeight = baseHeight + (lineCount * lineHeight);

        // Apply constraints
        calculatedHeight = Math.max(minHeight, Math.min(maxHeight, calculatedHeight));

        textArea.setPrefHeight(calculatedHeight);
    }

    private int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 1;
        }

        // Count line breaks and estimate wrapped lines
        String[] lines = text.split("\n");
        int totalLines = lines.length;

        // Estimate wrapped lines (assuming ~80 characters per line)
        for (String line : lines) {
            if (line.length() > 80) {
                totalLines += Math.ceil(line.length() / 80.0) - 1;
            }
        }

        return Math.max(1, totalLines);
    }

    private void updateWordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            wordCountLabel.setText("0 words");
            return;
        }

        int wordCount = text.trim().split("\\s+").length;
        wordCountLabel.setText(wordCount + " words");

        // Update status based on content length
        if (wordCount == 0) {
            updateStatus("✨ Ready to capture dreams...");
        } else if (wordCount < 50) {
            updateStatus("📝 Good start! Add more details about emotions and symbols.");
        } else if (wordCount < 200) {
            updateStatus("🌟 Excellent detail! Ready for deep analysis.");
        } else {
            updateStatus("💫 Comprehensive description! Perfect for analysis.");
        }
    }

    private void initializeDreamHistory() {
        // Initialize filter options
        filterComboBox.getItems().addAll(
                "All Dreams",
                "Last 7 Days",
                "Last 30 Days",
                "High Lucidity",
                "Vivid Dreams"
        );
        filterComboBox.setValue("All Dreams");

        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshDreamHistory();
        });

        // Set up filter listener
        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            refreshDreamHistory();
        });

        // Load initial dream history
        refreshDreamHistory();
    }

    // ===== EVENT HANDLERS =====

    @FXML
    private void handleSaveDream() {
        try {
            String dreamText = dreamTextArea.getText().trim();
            if (dreamText.isEmpty()) {
                showAlert("Error", "Please enter your dream description.");
                return;
            }

            DreamEntry dream = new DreamEntry(
                    1, // Default user ID
                    dreamText,
                    dreamDatePicker.getValue(),
                    sleepQualitySpinner.getValue(),
                    luciditySpinner.getValue()
            );

            int dreamId = databaseService.saveDreamEntry(dream);
            updateStatus("💾 Dream saved successfully! (ID: " + dreamId + ")");
            refreshDreamHistory();

            // Update analytics preview after saving a new dream
            updateAnalyticsPreview();

        } catch (Exception e) {
            showAlert("Error", "Failed to save dream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnalyzeDream() {
        try {
            String dreamText = dreamTextArea.getText().trim();
            if (dreamText.isEmpty()) {
                showAlert("Error", "Please enter your dream description first.");
                return;
            }

            // Show progress indicator
            analysisProgress.setVisible(true);
            updateStatus("🔮 Analyzing your dream with AI...");

            // Create a temporary dream entry for analysis
            DreamEntry dreamEntry = new DreamEntry(
                    1, // user ID
                    dreamText,
                    dreamDatePicker.getValue(),
                    sleepQualitySpinner.getValue(),
                    luciditySpinner.getValue()
            );

            // Run analysis in background thread
            new Thread(() -> {
                try {
                    // Use the advanced analysis service
                    DreamAnalysisService analysisService = new DreamAnalysisService();
                    AnalysisResult result = analysisService.performFullAnalysis(dreamEntry);

                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        analysisResultArea.setText(result.getInterpretationText());
                        analysisProgress.setVisible(false);
                        updateStatus("✨ Analysis complete! Insights ready.");

                        // Auto-resize the analysis result area
                        updateTextAreaSize(analysisResultArea, result.getInterpretationText());

                        // Refresh history to show the newly saved dream
                        refreshDreamHistory();

                        // Update analytics preview
                        updateAnalyticsPreview();
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        analysisProgress.setVisible(false);
                        showAlert("Analysis Error", "Failed to analyze dream: " + e.getMessage());
                        updateStatus("❌ Analysis failed.");
                        e.printStackTrace();
                    });
                }
            }).start();

        } catch (Exception e) {
            analysisProgress.setVisible(false);
            showAlert("Error", "Failed to start analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        dreamTextArea.clear();
        analysisResultArea.clear();
        dreamDatePicker.setValue(LocalDate.now());
        sleepQualitySpinner.getValueFactory().setValue(5);
        luciditySpinner.getValueFactory().setValue(0);

        // Clear generated image
        if (dreamVisualizationView != null) {
            dreamVisualizationView.setImage(null);
        }
        if (imageGenerationStatus != null) {
            imageGenerationStatus.setText("🌟 Ready to create magic");
        }
        if (imageGenerationProgress != null) {
            imageGenerationProgress.setProgress(0);
            imageGenerationProgress.setVisible(false);
        }
        if (cancelImageButton != null) {
            cancelImageButton.setVisible(false);
        }

        // Reset text area sizes
        updateTextAreaSize(dreamTextArea, "");
        updateTextAreaSize(analysisResultArea, "");

        updateStatus("🧹 Form cleared. Ready for new dream!");
    }

    @FXML
    private void handleRefreshHistory() {
        refreshDreamHistory();
        updateStatus("🔄 Dream history refreshed!");

        // Also update analytics preview when refreshing history
        updateAnalyticsPreview();
    }

    @FXML
    private void handleDreamSelected() {
        String selectedItem = dreamHistoryList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                // Extract dream ID from the selected item (assuming format: "ID - Date - Preview")
                String[] parts = selectedItem.split(" - ");
                if (parts.length >= 2) {
                    int dreamId = Integer.parseInt(parts[0]);

                    // Find the corresponding dream
                    List<DreamEntry> dreams = databaseService.getAllDreams(1);
                    for (DreamEntry dream : dreams) {
                        if (dream.getId() == dreamId) {
                            // Load the dream into the form
                            dreamTextArea.setText(dream.getDreamText());
                            dreamDatePicker.setValue(dream.getDreamDate());
                            sleepQualitySpinner.getValueFactory().setValue(dream.getSleepQuality());
                            luciditySpinner.getValueFactory().setValue(dream.getLucidityLevel());

                            // Update text area size
                            updateTextAreaSize(dreamTextArea, dream.getDreamText());

                            // Switch to the journal tab
                            mainTabPane.getSelectionModel().select(dreamJournalTab);
                            updateStatus("📖 Dream loaded from history!");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to load selected dream: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGenerateImage() {
        try {
            String dreamText = dreamTextArea.getText().trim();
            if (dreamText.isEmpty()) {
                showAlert("Error", "Please enter your dream description first.");
                return;
            }

            // Get analysis results
            Map<String, Double> emotions = nlpService.analyzeEmotion(dreamText);
            List<String> symbols = nlpService.extractSymbols(dreamText);

            // Cancel existing service if running
            if (visualizationService.isRunning()) {
                visualizationService.cancel();
            }

            // Reset service if it's not in READY state
            if (visualizationService.getState() != Worker.State.READY) {
                visualizationService.reset();
            }

            // Configure service with parameters
            visualizationService.setParameters(dreamText, emotions, symbols);

            // Setup UI state
            if (imageGenerationProgress != null) {
                imageGenerationProgress.setVisible(true);
                imageGenerationProgress.progressProperty().bind(visualizationService.progressProperty());
            }
            if (imageGenerationStatus != null) {
                imageGenerationStatus.textProperty().bind(visualizationService.messageProperty());
            }
            if (cancelImageButton != null) {
                cancelImageButton.setVisible(true);
            }
            if (generateImageButton != null) {
                generateImageButton.setDisable(true);
            }

            // Handle successful completion
            visualizationService.setOnSucceeded(e -> {
                Image generatedImage = visualizationService.getValue();
                if (generatedImage != null) {
                    dreamVisualizationView.setImage(generatedImage);
                    updateStatus("🎨 Dream visualization generated successfully!");
                } else {
                    showAlert("Error", "Failed to load generated image.");
                }
                resetImageGeneration();
            });

            // Handle failure
            visualizationService.setOnFailed(e -> {
                Throwable exception = visualizationService.getException();
                showAlert("Image Generation Error",
                        "Failed to generate dream visualization:\n" +
                                (exception != null ? exception.getMessage() : "Unknown error"));
                resetImageGeneration();
            });

            // Handle cancellation
            visualizationService.setOnCancelled(e -> {
                updateStatus("⏹️ Image generation cancelled.");
                resetImageGeneration();
            });

            // Start the service
            visualizationService.start();
            updateStatus("🌈 Generating cosmic art from your dream...");

        } catch (Exception e) {
            showAlert("Error", "Failed to start image generation: " + e.getMessage());
            resetImageGeneration();
        }
    }

    @FXML
    private void handleCancelImageGeneration() {
        if (visualizationService != null && visualizationService.isRunning()) {
            visualizationService.cancel();
            updateStatus("Cancelling image generation...");
        }
    }

    @FXML
    private void handleNewDream() {
        mainTabPane.getSelectionModel().select(dreamJournalTab);
        handleClear();
    }

    @FXML
    private void handleViewAnalysis() {
        if (!analysisResultArea.getText().isEmpty()) {
            mainTabPane.getSelectionModel().select(dreamJournalTab);
            updateStatus("📊 Viewing current analysis results");
        } else {
            showAlert("No Analysis", "Please analyze a dream first to view results.");
        }
    }

    @FXML
    private void handleShowAnalytics() {
        try {
            // Try to load the analytics dashboard
            URL analyticsUrl = getClass().getResource("/fxml/analytics-dashboard.fxml");
            if (analyticsUrl == null) {
                showAlert("Feature Not Available",
                        "The analytics dashboard is not available yet. This feature will be available in the next update.\n\n" +
                                "In the meantime, you can view your analytics preview in the Analytics tab.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(analyticsUrl);
            Parent analyticsRoot = loader.load();

            Stage analyticsStage = new Stage();
            analyticsStage.setTitle("Lucidia - Dream Analytics Dashboard");
            analyticsStage.setScene(new Scene(analyticsRoot, 1200, 800));
            analyticsStage.setMinWidth(1000);
            analyticsStage.setMinHeight(700);

            // Add custom styles if available
            try {
                URL cssUrl = getClass().getResource("/css/analytics.css");
                if (cssUrl != null) {
                    analyticsStage.getScene().getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                // CSS not available, continue without it
                System.out.println("Analytics CSS not available: " + e.getMessage());
            }

            analyticsStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error",
                    "Could not open analytics dashboard: " + e.getMessage() +
                            "\n\nPlease make sure all analytics files are properly installed.");
        }
    }

    @FXML
    private void handleExit() {
        // Cancel any running services before exit
        if (visualizationService != null && visualizationService.isRunning()) {
            visualizationService.cancel();
        }

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Lucidia");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved dreams will be lost.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            System.exit(0);
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Reset image generation UI state
     */
    private void resetImageGeneration() {
        // Unbind progress properties
        if (imageGenerationProgress != null) {
            imageGenerationProgress.progressProperty().unbind();
            imageGenerationProgress.setProgress(0);
            imageGenerationProgress.setVisible(false);
        }

        if (imageGenerationStatus != null) {
            imageGenerationStatus.textProperty().unbind();
            imageGenerationStatus.setText("🌟 Ready to create magic");
        }

        if (cancelImageButton != null) {
            cancelImageButton.setVisible(false);
        }

        if (generateImageButton != null) {
            generateImageButton.setDisable(false);
        }
    }

    /**
     * Load dream history from database
     */
    private void refreshDreamHistory() {
        try {
            String searchText = searchField.getText();
            String filter = filterComboBox.getValue();

            List<DreamEntry> dreams = databaseService.getDreamEntries(searchText, filter, 1);

            // Format dreams for display: "ID - Date - Sleep Quality/Lucidity - Preview"
            dreamHistoryList.setItems(FXCollections.observableArrayList(
                    dreams.stream()
                            .map(dream -> String.format("%d - %s - 😴%d/10 🌟%d/5 - %s",
                                    dream.getId(),
                                    dream.getDreamDate().toString(),
                                    dream.getSleepQuality(),
                                    dream.getLucidityLevel(),
                                    dream.getDreamText().length() > 50 ?
                                            dream.getDreamText().substring(0, 50) + "..." :
                                            dream.getDreamText()))
                            .toList()
            ));
        } catch (Exception e) {
            showAlert("Error", "Failed to load dream history: " + e.getMessage());
        }
    }

    /**
     * Display alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    // ===== GETTER METHODS FOR EXTERNAL ACCESS =====

    /**
     * Get current dream text
     */
    public String getDreamText() {
        return dreamTextArea.getText();
    }

    /**
     * Get current dream date
     */
    public LocalDate getDreamDate() {
        return dreamDatePicker.getValue();
    }

    /**
     * Get current sleep quality
     */
    public int getSleepQuality() {
        return sleepQualitySpinner.getValue();
    }

    /**
     * Get current lucidity level
     */
    public int getLucidityLevel() {
        return luciditySpinner.getValue();
    }

    /**
     * Get analysis results text
     */
    public String getAnalysisResults() {
        return analysisResultArea.getText();
    }

    /**
     * Check if a dream is currently loaded
     */
    public boolean hasDreamContent() {
        return dreamTextArea.getText() != null && !dreamTextArea.getText().trim().isEmpty();
    }

    /**
     * Get word count of current dream
     */
    public int getCurrentWordCount() {
        String text = dreamTextArea.getText();
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}