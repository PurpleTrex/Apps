package com.p2p.messaging;

import com.p2p.messaging.network.P2PNetworkManager;
import com.p2p.messaging.ui.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for P2P Messaging Service
 * Supports text, images, video, Excel files and other document types
 * Compatible with RCS, iOS and other messaging protocols
 */
public class P2PMessagingApp extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(P2PMessagingApp.class);
    private P2PNetworkManager networkManager;
    private MainWindow mainWindow;
    
    public static void main(String[] args) {
        logger.info("Starting P2P Messaging Application...");
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Initialize network manager
            networkManager = new P2PNetworkManager();
            
            // Initialize and show main window
            mainWindow = new MainWindow(networkManager);
            mainWindow.show(primaryStage);
            
            // Setup application shutdown hook
            primaryStage.setOnCloseRequest(event -> {
                shutdown();
                Platform.exit();
            });
            
            logger.info("P2P Messaging Application started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            Platform.exit();
        }
    }
    
    @Override
    public void stop() throws Exception {
        shutdown();
        super.stop();
    }
    
    private void shutdown() {
        logger.info("Shutting down P2P Messaging Application...");
        
        if (networkManager != null) {
            networkManager.shutdown();
        }
        
        if (mainWindow != null) {
            mainWindow.cleanup();
        }
        
        logger.info("Application shutdown complete");
    }
}