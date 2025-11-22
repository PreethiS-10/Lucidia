package com.lucidia.lucidia.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import com.lucidia.lucidia.controller.AnalysisController;

public class SceneNavigator {

    public static void loadScene(Stage stage, String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(SceneNavigator.class.getResource("/css/application.css").toExternalForm());
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    public static void switchToAnalysisScene(Stage stage, Object analysisData) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/fxml/analysis.fxml"));
        Parent root = loader.load();

        // Pass data to analysis controller
        AnalysisController controller = loader.getController();
        if (analysisData != null) {
            // You'll need to adapt this based on your data structure
            // controller.setAnalysisData(analysisData);
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(SceneNavigator.class.getResource("/css/application.css").toExternalForm());
        stage.setTitle("Lucidia - Dream Analysis");
        stage.setScene(scene);
        stage.show();
    }
}