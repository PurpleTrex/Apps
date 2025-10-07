package com.structurecreation;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Splash Screen for Gaia Project Structure Creator
 * Shows the application logo and loading progress during startup
 */
public class SplashScreen {

    private static final Logger logger = LoggerFactory.getLogger(SplashScreen.class);
    private Stage splashStage;
    private ProgressBar progressBar;
    private Label statusLabel;

    public SplashScreen() {
        createSplashScreen();
    }

    private void createSplashScreen() {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        
        VBox splashLayout = new VBox(20);
        splashLayout.setAlignment(Pos.CENTER);
        splashLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e); -fx-padding: 40;");
        
        try {
            // Load and display the app icon
            Image appIcon = new Image(getClass().getResourceAsStream("/images/app-icon.png"));
            ImageView imageView = new ImageView(appIcon);
            imageView.setFitWidth(128);
            imageView.setFitHeight(128);
            imageView.setPreserveRatio(true);
            splashLayout.getChildren().add(imageView);
            
        } catch (Exception e) {
            logger.warn("Could not load splash screen icon: {}", e.getMessage());
        }
        
        // Application title
        Label titleLabel = new Label("Gaia");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.WHITE);
        splashLayout.getChildren().add(titleLabel);
        
        // Subtitle
        Label subtitleLabel = new Label("Project Structure Creator");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        subtitleLabel.setTextFill(Color.LIGHTGRAY);
        splashLayout.getChildren().add(subtitleLabel);
        
        // Version label
        Label versionLabel = new Label("Version 1.0.0");
        versionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        versionLabel.setTextFill(Color.LIGHTGRAY);
        splashLayout.getChildren().add(versionLabel);
        
        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #3498db;");
        splashLayout.getChildren().add(progressBar);
        
        // Status label
        statusLabel = new Label("Initializing...");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.LIGHTGRAY);
        splashLayout.getChildren().add(statusLabel);
        
        Scene splashScene = new Scene(splashLayout, 400, 350);
        splashStage.setScene(splashScene);
        
        // Center the splash screen
        splashStage.centerOnScreen();
    }

    public void show() {
        Platform.runLater(() -> {
            splashStage.show();
            splashStage.toFront();
        });
    }

    public void updateProgress(double progress, String status) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            statusLabel.setText(status);
        });
    }

    public void close() {
        Platform.runLater(() -> {
            if (splashStage != null) {
                splashStage.close();
            }
        });
    }

    /**
     * Simulate loading process with progress updates
     */
    public Task<Void> createLoadingTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String[] loadingSteps = {
                    "Loading application resources...",
                    "Initializing services...",
                    "Loading UI components...",
                    "Preparing dependency system...",
                    "Finalizing startup..."
                };
                
                for (int i = 0; i < loadingSteps.length; i++) {
                    final int index = i;
                    final String step = loadingSteps[i];
                    Platform.runLater(() -> SplashScreen.this.updateProgress((double) index / loadingSteps.length, step));
                    Thread.sleep(400); // Simulate loading time
                }
                
                Platform.runLater(() -> SplashScreen.this.updateProgress(1.0, "Ready!"));
                Thread.sleep(200);
                
                return null;
            }
        };
    }
}