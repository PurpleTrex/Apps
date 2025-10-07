package com.structurecreation;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Application class for Gaia Project Structure Creator
 * A modern GUI tool for creating project folder structures and managing dependencies
 */
public class ProjectStructureCreatorApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(ProjectStructureCreatorApp.class);
    private static final String APP_TITLE = "Gaia";
    private static final String APP_SUBTITLE = "Project Structure Creator";
    private static final String APP_VERSION = "1.0.0";
    
    private SplashScreen splashScreen;

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Gaia v{}", APP_VERSION);
            
            // Show splash screen first
            splashScreen = new SplashScreen();
            splashScreen.show();
            
            // Create loading task
            Task<Void> loadingTask = splashScreen.createLoadingTask();
            
            // When loading is complete, show the main application
            loadingTask.setOnSucceeded(e -> {
                try {
                    showMainApplication(primaryStage);
                    splashScreen.close();
                } catch (Exception ex) {
                    logger.error("Failed to show main application", ex);
                    splashScreen.close();
                    throw new RuntimeException("Failed to show main application", ex);
                }
            });
            
            loadingTask.setOnFailed(e -> {
                logger.error("Loading task failed", loadingTask.getException());
                splashScreen.close();
                throw new RuntimeException("Loading task failed", loadingTask.getException());
            });
            
            // Start the loading task in background thread
            Thread loadingThread = new Thread(loadingTask);
            loadingThread.setDaemon(true);
            loadingThread.start();
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            if (splashScreen != null) {
                splashScreen.close();
            }
            throw new RuntimeException("Failed to start application", e);
        }
    }
    
    private void showMainApplication(Stage primaryStage) throws Exception {
        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        
        // Add CSS styling
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        // Configure stage
        primaryStage.setTitle(APP_TITLE + " - " + APP_SUBTITLE + " v" + APP_VERSION);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        
        // Set application icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/app-icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            logger.warn("Could not load application icon: {}", e.getMessage());
        }
        
        // Show the stage
        primaryStage.show();
        
        logger.info("Gaia application started successfully");
    }

    @Override
    public void stop() {
        logger.info("Application stopping...");
    }

    public static void main(String[] args) {
        logger.info("Launching Gaia Project Structure Creator...");
        launch(args);
    }
}