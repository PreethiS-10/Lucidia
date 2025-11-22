package com.lucidia.lucidia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DreamVisualizationService extends Service<Image> {

    private static final String PYTHON_SCRIPT_PATH = "dream_visualizer.py";
    private static final String OUTPUT_DIR = "generated_images";
    private static final int TIMEOUT_MINUTES = 10;

    private final ObjectMapper objectMapper;

    // Parameters for the task
    private String dreamText;
    private Map<String, Double> emotions;
    private List<String> symbols;

    public DreamVisualizationService() {
        this.objectMapper = new ObjectMapper();
        ensureOutputDirectory();
    }

    public void setParameters(String dreamText, Map<String, Double> emotions, List<String> symbols) {
        this.dreamText = dreamText;
        this.emotions = emotions;
        this.symbols = symbols;
    }

    @Override
    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                updateMessage("Preparing parameters...");
                updateProgress(0, 100);

                // Validate inputs
                if (dreamText == null || dreamText.trim().isEmpty()) {
                    throw new IllegalArgumentException("Dream text cannot be empty");
                }

                updateMessage("Creating parameter file...");
                updateProgress(10, 100);

                // Create temporary parameter file
                File paramFile = createParameterFile();

                try {
                    // Use parameter file instead of command line arguments
                    ProcessBuilder pb = new ProcessBuilder();
                    pb.command(
                            "python", "-u",
                            PYTHON_SCRIPT_PATH,
                            "--param-file", paramFile.getAbsolutePath(),
                            "--output-dir", OUTPUT_DIR
                    );

                    System.err.println("DEBUG: Command: " + String.join(" ", pb.command()));

                    pb.directory(new File("."));
                    pb.redirectErrorStream(false);

                    updateMessage("Starting AI image generation...");
                    updateProgress(25, 100);

                    Process process = pb.start();

                    // Monitor process output
                    StringBuilder errorOutput = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream()))) {

                        String line;
                        while ((line = reader.readLine()) != null && !isCancelled()) {
                            System.err.println("Python: " + line);
                            errorOutput.append(line).append("\n");

                            // Parse progress messages
                            if (line.contains("GENERATING")) {
                                updateMessage("Creating dream visualization...");
                                updateProgress(40, 100);
                            } else if (line.contains("PROCESSING")) {
                                updateMessage("Processing with AI...");
                                updateProgress(60, 100);
                            } else if (line.contains("DOWNLOADING")) {
                                updateMessage("Downloading generated image...");
                                updateProgress(80, 100);
                            } else if (line.contains("SAVED")) {
                                updateMessage("Saving image...");
                                updateProgress(95, 100);
                            }
                        }
                    }

                    // Handle cancellation
                    if (isCancelled()) {
                        process.destroyForcibly();
                        throw new InterruptedException("Task was cancelled");
                    }

                    boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);

                    if (!finished) {
                        process.destroyForcibly();
                        throw new RuntimeException("Generation timed out");
                    }

                    int exitCode = process.exitValue();

                    // Read success message
                    String output;
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {
                        output = reader.lines()
                                .filter(line -> line.startsWith("SUCCESS: "))
                                .findFirst()
                                .orElse(null);
                    }

                    if (exitCode != 0 || output == null) {
                        throw new RuntimeException("Python script failed with exit code " + exitCode +
                                "\nError: " + errorOutput.toString());
                    }

                    updateMessage("Loading generated image...");
                    updateProgress(98, 100);

                    String imagePath = output.substring(9); // Remove "SUCCESS: " prefix
                    Image image = loadGeneratedImage(imagePath);

                    updateMessage("Complete!");
                    updateProgress(100, 100);

                    return image;

                } finally {
                    // Clean up parameter file
                    if (paramFile.exists()) {
                        try {
                            paramFile.delete();
                        } catch (Exception e) {
                            System.err.println("Warning: Could not delete temp file");
                        }
                    }
                }
            }

            private File createParameterFile() throws IOException {
                File tempFile = File.createTempFile("dream_params_", ".json");

                Map<String, Object> params = Map.of(
                        "dream_text", dreamText.trim(),
                        "emotions", emotions != null ? emotions : Map.of(),
                        "symbols", symbols != null ? symbols : List.of()
                );

                try (FileWriter writer = new FileWriter(tempFile, java.nio.charset.StandardCharsets.UTF_8)) {
                    objectMapper.writeValue(writer, params);
                }

                System.err.println("DEBUG: Created param file: " + tempFile.getAbsolutePath());
                return tempFile;
            }
        };
    }

    private Image loadGeneratedImage(String imagePath) throws FileNotFoundException {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new FileNotFoundException("Generated image not found: " + imagePath);
        }

        try {
            return new Image(imageFile.toURI().toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load generated image: " + e.getMessage(), e);
        }
    }

    private void ensureOutputDirectory() {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create output directory: " + e.getMessage());
        }
    }
}
