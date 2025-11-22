package com.lucidia.lucidia.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.lucidia.lucidia.service.DreamAnalyticsService;
import com.lucidia.lucidia.model.DreamStatistics;
import com.lucidia.lucidia.model.DreamInsight;

import java.net.URL;
import java.util.*;

public class AnalyticsController implements Initializable {

    @FXML private Label totalDreamsLabel;
    @FXML private Label weeklyDreamsLabel;
    @FXML private Label sleepQualityLabel;
    @FXML private Label currentStreakLabel;
    @FXML private Label monthlyDreamsLabel;
    @FXML private Label avgLucidityLabel;
    @FXML private Label longestStreakLabel;
    @FXML private Label dominantEmotionLabel;
    @FXML private Label emotionDescription;

    @FXML private PieChart emotionChart;
    @FXML private BarChart<String, Number> symbolChart;

    @FXML private ListView<String> insightsList;
    @FXML private ListView<String> topSymbolsList;

    @FXML private Button refreshButton;

    private DreamAnalyticsService analyticsService;
    private final int currentUserId = 1; // Default user ID

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        analyticsService = new DreamAnalyticsService();
        setupCharts();
        loadAnalyticsData();
    }

    @FXML
    private void handleRefresh() {
        loadAnalyticsData();
        showAlert("Success", "Analytics dashboard refreshed!");
    }

    private void setupCharts() {
        try {
            // Setup emotion pie chart
            emotionChart.setLegendVisible(false);
            emotionChart.setLabelsVisible(true);
            emotionChart.setStyle("-fx-background-color: transparent;");

            // Setup symbol bar chart - axes are now defined in FXML
            if (symbolChart != null) {
                // Get the axes from the chart (they should be defined in FXML)
                CategoryAxis xAxis = (CategoryAxis) symbolChart.getXAxis();
                NumberAxis yAxis = (NumberAxis) symbolChart.getYAxis();

                if (xAxis != null) {
                    xAxis.setLabel("Symbols");
                    xAxis.setStyle("-fx-tick-label-fill: #DDA0DD; -fx-font-size: 12px;");
                }

                if (yAxis != null) {
                    yAxis.setLabel("Frequency");
                    yAxis.setStyle("-fx-tick-label-fill: #DDA0DD; -fx-font-size: 12px;");
                }

                symbolChart.setLegendVisible(false);
                symbolChart.setTitle("");
                symbolChart.setStyle("-fx-background-color: transparent;");
            }
        } catch (Exception e) {
            System.err.println("Error setting up charts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAnalyticsData() {
        try {
            DreamStatistics stats = analyticsService.generateUserStatistics(currentUserId);
            if (stats != null) {
                updateStatisticsCards(stats);
                updateCharts(stats);
                updateInsights();
                updateDetailedStats(stats);
            } else {
                // Initialize with sample data if stats is null
                initializeWithSampleData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load analytics data: " + e.getMessage());
            // Initialize with sample data for demonstration
            initializeWithSampleData();
        }
    }

    private void updateStatisticsCards(DreamStatistics stats) {
        try {
            if (totalDreamsLabel != null) totalDreamsLabel.setText(String.valueOf(stats.getTotalDreams()));
            if (weeklyDreamsLabel != null) weeklyDreamsLabel.setText(String.valueOf(stats.getDreamsThisWeek()));
            if (sleepQualityLabel != null) sleepQualityLabel.setText(String.format("%.1f", stats.getAverageSleepQuality()));
            if (currentStreakLabel != null) currentStreakLabel.setText(String.valueOf(stats.getCurrentStreak()));
        } catch (Exception e) {
            System.err.println("Error updating statistics cards: " + e.getMessage());
        }
    }

    private void updateCharts(DreamStatistics stats) {
        try {
            // Update emotion chart
            ObservableList<PieChart.Data> emotionData = FXCollections.observableArrayList();
            if (stats.getEmotionFrequency() != null && !stats.getEmotionFrequency().isEmpty()) {
                stats.getEmotionFrequency().forEach((emotion, count) -> {
                    if (count > 0) {
                        emotionData.add(new PieChart.Data(emotion + " (" + count + ")", count));
                    }
                });
            } else {
                // Sample data if no real data available
                emotionData.addAll(
                        new PieChart.Data("Joy (3)", 3),
                        new PieChart.Data("Peace (2)", 2),
                        new PieChart.Data("Anxiety (1)", 1)
                );
            }
            emotionChart.setData(emotionData);

            // Update symbol chart
            if (symbolChart != null) {
                symbolChart.getData().clear();

                XYChart.Series<String, Number> symbolSeries = new XYChart.Series<>();
                symbolSeries.setName("Symbol Frequency");

                if (stats.getSymbolFrequency() != null && !stats.getSymbolFrequency().isEmpty()) {
                    // Get top 8 symbols
                    stats.getSymbolFrequency().entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .limit(8)
                            .forEach(entry -> {
                                symbolSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                            });
                } else {
                    // Sample data if no real data available
                    symbolSeries.getData().addAll(
                            new XYChart.Data<>("Water", 5),
                            new XYChart.Data<>("Flying", 3),
                            new XYChart.Data<>("House", 2),
                            new XYChart.Data<>("Car", 2),
                            new XYChart.Data<>("School", 1)
                    );
                }

                symbolChart.getData().add(symbolSeries);
            }
        } catch (Exception e) {
            System.err.println("Error updating charts: " + e.getMessage());
            e.printStackTrace();
            initializeWithSampleCharts();
        }
    }

    private void updateInsights() {
        try {
            List<DreamInsight> insights = analyticsService.generatePersonalizedInsights(currentUserId);
            ObservableList<String> insightItems = FXCollections.observableArrayList();

            if (insights != null && !insights.isEmpty()) {
                for (DreamInsight insight : insights) {
                    String insightText = String.format("💡 %s\n%s", insight.getTitle(), insight.getDescription());
                    insightItems.add(insightText);
                }
            } else {
                // Sample insights if no real insights available
                insightItems.addAll(
                        "💡 Start recording dreams regularly to unlock personalized insights",
                        "🌟 Try keeping a dream journal by your bed for better recall",
                        "📈 Your dream patterns will become clearer with more entries"
                );
            }

            if (insightsList != null) {
                insightsList.setItems(insightItems);

                // Custom cell factory for better formatting
                insightsList.setCellFactory(param -> new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setStyle("-fx-text-fill: #F0F8FF; -fx-font-size: 13px; -fx-padding: 10; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");
                            setWrapText(true);
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error updating insights: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateDetailedStats(DreamStatistics stats) {
        try {
            // Add null checks for all labels
            if (monthlyDreamsLabel != null) {
                monthlyDreamsLabel.setText(String.valueOf(stats.getDreamsThisMonth()));
            }
            if (avgLucidityLabel != null) {
                avgLucidityLabel.setText(String.format("%.1f", stats.getAverageLucidity()));
            }
            if (longestStreakLabel != null) {
                longestStreakLabel.setText(stats.getLongestDreamStreak() + " days");
            }
            if (dominantEmotionLabel != null) {
                dominantEmotionLabel.setText(stats.getMostCommonEmotion());
            }

            // Update emotion description with null check
            if (emotionDescription != null) {
                String emotion = stats.getMostCommonEmotion();
                String description = getEmotionDescription(emotion);
                emotionDescription.setText(description);
            }

            // Update top symbols list with null check
            if (topSymbolsList != null) {
                ObservableList<String> topSymbols = FXCollections.observableArrayList();
                if (stats.getTopSymbols() != null && !stats.getTopSymbols().isEmpty()) {
                    int rank = 1;
                    for (String symbol : stats.getTopSymbols()) {
                        topSymbols.add(rank + ". " + symbol);
                        rank++;
                    }
                } else {
                    topSymbols.add("No symbols recorded yet");
                }
                topSymbolsList.setItems(topSymbols);
            }

        } catch (Exception e) {
            System.err.println("Error updating detailed stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeWithSampleData() {
        try {
            // Set sample data for all UI components with null checks
            if (totalDreamsLabel != null) totalDreamsLabel.setText("5");
            if (weeklyDreamsLabel != null) weeklyDreamsLabel.setText("2");
            if (sleepQualityLabel != null) sleepQualityLabel.setText("7.5");
            if (currentStreakLabel != null) currentStreakLabel.setText("3");
            if (monthlyDreamsLabel != null) monthlyDreamsLabel.setText("5");
            if (avgLucidityLabel != null) avgLucidityLabel.setText("2.5");
            if (longestStreakLabel != null) longestStreakLabel.setText("5 days");
            if (dominantEmotionLabel != null) dominantEmotionLabel.setText("Joy");
            if (emotionDescription != null) emotionDescription.setText("Your dreams show positive emotional patterns");

            // Initialize charts with sample data
            initializeWithSampleCharts();

            // Initialize insights list
            if (insightsList != null) {
                ObservableList<String> sampleInsights = FXCollections.observableArrayList(
                        "💡 You're remembering more dreams this week!",
                        "🌟 Try reality checks to increase lucidity",
                        "📈 Your sleep quality has improved recently"
                );
                insightsList.setItems(sampleInsights);
            }

            // Initialize top symbols
            if (topSymbolsList != null) {
                ObservableList<String> sampleSymbols = FXCollections.observableArrayList(
                        "1. Water",
                        "2. Flying",
                        "3. House",
                        "4. Car",
                        "5. School"
                );
                topSymbolsList.setItems(sampleSymbols);
            }
        } catch (Exception e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeWithSampleCharts() {
        try {
            // Sample emotion data
            ObservableList<PieChart.Data> emotionData = FXCollections.observableArrayList(
                    new PieChart.Data("Joy (3)", 3),
                    new PieChart.Data("Peace (2)", 2),
                    new PieChart.Data("Anxiety (1)", 1)
            );
            emotionChart.setData(emotionData);

            // Sample symbol data
            if (symbolChart != null) {
                symbolChart.getData().clear();
                XYChart.Series<String, Number> symbolSeries = new XYChart.Series<>();
                symbolSeries.setName("Symbol Frequency");
                symbolSeries.getData().addAll(
                        new XYChart.Data<>("Water", 5),
                        new XYChart.Data<>("Flying", 3),
                        new XYChart.Data<>("House", 2)
                );
                symbolChart.getData().add(symbolSeries);
            }
        } catch (Exception e) {
            System.err.println("Error initializing sample charts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getEmotionDescription(String emotion) {
        if (emotion == null || emotion.equalsIgnoreCase("No data")) {
            return "Start recording dreams to see emotional patterns";
        }

        switch (emotion.toLowerCase()) {
            case "joy":
                return "Your dreams are filled with positive energy and happiness";
            case "fear":
                return "Anxiety and fear are common themes in your dream world";
            case "sadness":
                return "Your dreams often reflect melancholic or emotional states";
            case "anger":
                return "Strong emotions and conflicts appear frequently";
            case "surprise":
                return "Your dreams are unpredictable and full of unexpected turns";
            case "neutral":
                return "Your dreams maintain a balanced emotional tone";
            default:
                return "Your dream emotions show unique patterns worth exploring";
        }
    }

    private void showAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Customize alert dialog
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #1a1a2e; " +
                    "-fx-text-fill: #F0F8FF; -fx-border-color: #8A2BE2; -fx-border-width: 2;");

            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing alert: " + e.getMessage());
        }
    }
}