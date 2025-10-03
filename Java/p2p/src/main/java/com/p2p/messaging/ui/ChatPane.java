package com.p2p.messaging.ui;

import com.p2p.messaging.model.Message;
import com.p2p.messaging.model.Peer;
import com.p2p.messaging.network.P2PNetworkManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Chat pane for individual conversations
 * Supports sending text messages and various file types
 */
public class ChatPane {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatPane.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private final Peer peer;
    private final P2PNetworkManager networkManager;
    
    // UI Components
    private VBox root;
    private ScrollPane messageScrollPane;
    private VBox messageContainer;
    private TextArea messageInput;
    private Button sendButton;
    private Button attachButton;
    
    public ChatPane(Peer peer, P2PNetworkManager networkManager) {
        this.peer = peer;
        this.networkManager = networkManager;
        
        createUI();
        setupEventHandlers();
        loadMessageHistory();
    }
    
    private void createUI() {
        root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");
        
        // Chat header
        HBox header = createChatHeader();
        
        // Message area
        messageContainer = new VBox(10);
        messageContainer.setPadding(new Insets(10));
        
        messageScrollPane = new ScrollPane(messageContainer);
        messageScrollPane.setFitToWidth(true);
        messageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messageScrollPane.setStyle("-fx-background-color: #f8f9fa;");
        
        // Input area
        HBox inputArea = createInputArea();
        
        root.getChildren().addAll(header, messageScrollPane, inputArea);
        VBox.setVgrow(messageScrollPane, Priority.ALWAYS);
    }
    
    private HBox createChatHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 5;");
        
        Label statusIcon = new Label(peer.getStatusIcon());
        statusIcon.setFont(Font.font(16));
        
        VBox peerInfo = new VBox(2);
        Label nameLabel = new Label(peer.getDisplayName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Label statusLabel = new Label(peer.getStatus().toString().toLowerCase());
        statusLabel.setFont(Font.font("Arial", 10));
        statusLabel.setTextFill(Color.GRAY);
        
        peerInfo.getChildren().addAll(nameLabel, statusLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button callButton = new Button("📞");
        Button videoButton = new Button("📹");
        Button infoButton = new Button("ℹ️");
        
        header.getChildren().addAll(statusIcon, peerInfo, spacer, callButton, videoButton, infoButton);
        return header;
    }
    
    private HBox createInputArea() {
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.BOTTOM_CENTER);
        inputArea.setPadding(new Insets(10));
        inputArea.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        
        // Attach button
        attachButton = new Button("📎");
        attachButton.setTooltip(new Tooltip("Attach file"));
        attachButton.setPrefSize(40, 40);
        
        // Message input
        messageInput = new TextArea();
        messageInput.setPromptText("Type a message...");
        messageInput.setPrefRowCount(2);
        messageInput.setMaxHeight(80);
        messageInput.setWrapText(true);
        messageInput.setStyle("-fx-border-color: #ced4da; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Send button
        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setPrefSize(80, 40);
        sendButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 5;");
        
        // Emoji button
        Button emojiButton = new Button("😊");
        emojiButton.setTooltip(new Tooltip("Insert emoji"));
        emojiButton.setPrefSize(40, 40);
        
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        inputArea.getChildren().addAll(attachButton, messageInput, emojiButton, sendButton);
        
        return inputArea;
    }
    
    private void setupEventHandlers() {
        sendButton.setOnAction(e -> sendMessage());
        
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                sendMessage();
            }
        });
        
        attachButton.setOnAction(e -> showFileChooser());
    }
    
    private void sendMessage() {
        String text = messageInput.getText().trim();
        
        if (!text.isEmpty()) {
            Message message = new Message(
                networkManager.getLocalPeer().getId(),
                peer.getId(),
                text
            );
            
            if (networkManager.sendMessage(message)) {
                addMessage(message);
                messageInput.clear();
                messageInput.requestFocus();
            } else {
                showAlert("Failed to send message. Peer may be offline.");
            }
        }
    }
    
    private void showFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        
        // Add file type filters
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.avi", "*.mov", "*.wmv", "*.flv"),
            new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav", "*.ogg", "*.m4a"),
            new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt"),
            new FileChooser.ExtensionFilter("Spreadsheets", "*.xls", "*.xlsx", "*.csv")
        );
        
        File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        
        if (selectedFile != null) {
            sendFile(selectedFile);
        }
    }
    
    private void sendFile(File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
            
            Message.MessageType messageType = determineMessageType(extension);
            
            Message message = new Message(
                networkManager.getLocalPeer().getId(),
                peer.getId(),
                file.getName(),
                fileData,
                messageType
            );
            
            if (networkManager.sendMessage(message)) {
                addMessage(message);
                showStatus("File sent: " + file.getName());
            } else {
                showAlert("Failed to send file. Peer may be offline.");
            }
            
        } catch (IOException e) {
            logger.error("Failed to read file: {}", file.getName(), e);
            showAlert("Failed to read file: " + e.getMessage());
        }
    }
    
    private Message.MessageType determineMessageType(String extension) {
        switch (extension) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
                return Message.MessageType.IMAGE;
                
            case "mp4":
            case "avi":
            case "mov":
            case "wmv":
            case "flv":
                return Message.MessageType.VIDEO;
                
            case "mp3":
            case "wav":
            case "ogg":
            case "m4a":
                return Message.MessageType.AUDIO;
                
            case "pdf":
            case "doc":
            case "docx":
            case "txt":
            case "xls":
            case "xlsx":
            case "csv":
                return Message.MessageType.DOCUMENT;
                
            default:
                return Message.MessageType.FILE;
        }
    }
    
    public void addMessage(Message message) {
        Platform.runLater(() -> {
            Node messageNode = createMessageNode(message);
            messageContainer.getChildren().add(messageNode);
            
            // Auto-scroll to bottom
            Platform.runLater(() -> {
                messageScrollPane.setVvalue(1.0);
            });
        });
    }
    
    private Node createMessageNode(Message message) {
        boolean isOwnMessage = message.getSenderId().equals(networkManager.getLocalPeer().getId());
        
        HBox messageBox = new HBox();
        messageBox.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        VBox messageBubble = new VBox(5);
        messageBubble.setPadding(new Insets(10));
        messageBubble.setMaxWidth(400);
        
        if (isOwnMessage) {
            messageBubble.setStyle("-fx-background-color: #007bff; -fx-background-radius: 15; -fx-text-fill: white;");
        } else {
            messageBubble.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 15;");
        }
        
        // Message content
        Label contentLabel = new Label(message.getDisplayText());
        contentLabel.setWrapText(true);
        contentLabel.setFont(Font.font("Arial", 13));
        
        if (isOwnMessage) {
            contentLabel.setTextFill(Color.WHITE);
        }
        
        // Timestamp
        Label timeLabel = new Label(message.getTimestamp().format(TIME_FORMATTER));
        timeLabel.setFont(Font.font("Arial", 9));
        timeLabel.setTextFill(isOwnMessage ? Color.LIGHTGRAY : Color.GRAY);
        
        messageBubble.getChildren().addAll(contentLabel, timeLabel);
        
        // Add delivery/read indicators for own messages
        if (isOwnMessage) {
            Label statusLabel = new Label(message.isRead() ? "✓✓" : message.isDelivered() ? "✓" : "○");
            statusLabel.setFont(Font.font("Arial", 8));
            statusLabel.setTextFill(Color.LIGHTGRAY);
            messageBubble.getChildren().add(statusLabel);
        }
        
        messageBox.getChildren().add(messageBubble);
        return messageBox;
    }
    
    private void loadMessageHistory() {
        List<Message> history = networkManager.getMessageHistory();
        
        for (Message message : history) {
            if (message.getSenderId().equals(peer.getId()) || 
                message.getRecipientId().equals(peer.getId())) {
                addMessage(message);
            }
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showStatus(String status) {
        // Could implement status bar updates here
        logger.info(status);
    }
    
    public VBox getRoot() {
        return root;
    }
    
    public void cleanup() {
        // Cleanup resources if needed
    }
}