package com.structurecreation.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Utility class for showing various types of alerts and dialogs
 */
public class AlertUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertUtils.class);
    
    /**
     * Shows an information alert
     */
    public static void showInformation(String title, String message) {
        logger.info("Showing info alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows a warning alert
     */
    public static void showWarning(String title, String message) {
        logger.warn("Showing warning alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an error alert
     */
    public static void showError(String title, String message) {
        logger.error("Showing error alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an error alert with exception details
     */
    public static void showError(String title, String message, Throwable exception) {
        logger.error("Showing error alert with exception: {} - {}", title, message, exception);
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage());
        
        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();
        
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        
        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        
        alert.showAndWait();
    }
    
    /**
     * Shows a confirmation dialog and returns the user's choice
     */
    public static boolean showConfirmation(String title, String message) {
        logger.info("Showing confirmation dialog: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        boolean confirmed = result.isPresent() && result.get() == ButtonType.OK;
        logger.info("Confirmation result: {}", confirmed);
        return confirmed;
    }
    
    /**
     * Shows a confirmation dialog with custom button text
     */
    public static boolean showConfirmation(String title, String message, String confirmButtonText, String cancelButtonText) {
        logger.info("Showing custom confirmation dialog: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        ButtonType confirmButton = new ButtonType(confirmButtonText);
        ButtonType cancelButton = new ButtonType(cancelButtonText);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        boolean confirmed = result.isPresent() && result.get() == confirmButton;
        logger.info("Custom confirmation result: {}", confirmed);
        return confirmed;
    }
    
    /**
     * Shows a success alert with green styling
     */
    public static void showSuccess(String title, String message) {
        logger.info("Showing success alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Success!");
        alert.setContentText(message);
        
        // Add success styling
        alert.getDialogPane().getStyleClass().add("success-alert");
        
        alert.showAndWait();
    }
    
    /**
     * Shows a progress-style information alert
     */
    public static void showProgress(String title, String message) {
        logger.info("Showing progress alert: {} - {}", title, message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Please Wait...");
        alert.setContentText(message);
        
        // Remove buttons to make it non-dismissible
        alert.getButtonTypes().clear();
        
        alert.show();
    }
    
    /**
     * Creates a reusable alert instance for repeated use
     */
    public static Alert createAlert(Alert.AlertType type, String title, String headerText, String contentText) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        return alert;
    }
    
    /**
     * Shows a detailed information alert with expandable content
     */
    public static void showDetailedInformation(String title, String message, String details) {
        logger.info("Showing detailed info alert: {} - {}", title, message);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }
}