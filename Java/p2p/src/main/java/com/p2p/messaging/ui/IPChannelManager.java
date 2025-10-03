package com.p2p.messaging.ui;

import com.p2p.messaging.network.IPChannelService;
import com.p2p.messaging.network.IPChannelService.IPChannel;
import com.p2p.messaging.network.IPChannelService.IPChannelConfig;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IP Channel Manager - GUI for creating and managing public messaging channels
 */
public class IPChannelManager {
    
    private final IPChannelService channelService;
    private ScheduledExecutorService scheduler;
    
    private Stage stage;
    private TableView<ChannelRow> channelTable;
    private Label statusLabel;
    
    public IPChannelManager(IPChannelService channelService) {
        this.channelService = channelService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    public void show() {
        if (stage != null && stage.isShowing()) {
            stage.toFront();
            return;
        }
        
        createAndShowWindow();
        
        // Create new scheduler if the old one is shutdown
        if (scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }
        startPeriodicRefresh();
    }
    
    private void createAndShowWindow() {
        stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setTitle("📡 IP Channel Manager - Public Messaging URLs");
        stage.setWidth(800);
        stage.setHeight(600);
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);");
        
        // Header
        VBox header = createHeader();
        
        // Control buttons
        HBox controls = createControlButtons();
        
        // Channel table
        VBox tableContainer = createChannelTable();
        
        // Status bar
        statusLabel = new Label("Ready to create IP channels");
        statusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        root.getChildren().addAll(header, controls, tableContainer, statusLabel);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
        // Initial refresh
        refreshChannelList();
        
        stage.setOnCloseRequest(e -> {
            scheduler.shutdown();
        });
    }
    
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        
        Label title = new Label("📡 IP Channel Manager");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label subtitle = new Label("Create public URLs that anyone can use to send you messages");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        Separator separator = new Separator();
        separator.setMaxWidth(400);
        
        header.getChildren().addAll(title, subtitle, separator);
        return header;
    }
    
    private HBox createControlButtons() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));
        
        Button createPublicBtn = new Button("🌐 Create Public Channel");
        createPublicBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        createPublicBtn.setOnAction(e -> showCreatePublicChannelDialog());
        
        Button createTempBtn = new Button("⏰ Create Temporary Channel");
        createTempBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        createTempBtn.setOnAction(e -> showCreateTempChannelDialog());
        
        Button createSecureBtn = new Button("🔒 Create Secure Channel");
        createSecureBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        createSecureBtn.setOnAction(e -> showCreateSecureChannelDialog());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> refreshChannelList());
        
        controls.getChildren().addAll(createPublicBtn, createTempBtn, createSecureBtn, refreshBtn);
        return controls;
    }
    
    private VBox createChannelTable() {
        VBox container = new VBox(10);
        
        Label tableTitle = new Label("Active Channels");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        channelTable = new TableView<>();
        channelTable.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5px;");
        
        // Create columns
        TableColumn<ChannelRow, String> nameCol = new TableColumn<>("Channel Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        TableColumn<ChannelRow, String> urlCol = new TableColumn<>("Public URL");
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
        urlCol.setPrefWidth(300);
        
        TableColumn<ChannelRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);
        
        TableColumn<ChannelRow, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("created"));
        createdCol.setPrefWidth(120);
        
        TableColumn<ChannelRow, String> messagesCol = new TableColumn<>("Messages");
        messagesCol.setCellValueFactory(new PropertyValueFactory<>("messages"));
        messagesCol.setPrefWidth(80);
        
        TableColumn<ChannelRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        actionsCol.setCellFactory(col -> new TableCell<ChannelRow, Void>() {
            private final Button copyBtn = new Button("📋");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(5, copyBtn, deleteBtn);
            
            {
                copyBtn.setTooltip(new Tooltip("Copy URL"));
                deleteBtn.setTooltip(new Tooltip("Delete Channel"));
                copyBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px;");
                buttons.setAlignment(Pos.CENTER);
                
                copyBtn.setOnAction(e -> {
                    ChannelRow row = getTableRow().getItem();
                    if (row != null) {
                        copyToClipboard(row.getUrl());
                        showNotification("URL copied to clipboard!");
                    }
                });
                
                deleteBtn.setOnAction(e -> {
                    ChannelRow row = getTableRow().getItem();
                    if (row != null) {
                        deleteChannel(row.getId());
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        channelTable.getColumns().add(nameCol);
        channelTable.getColumns().add(urlCol);
        channelTable.getColumns().add(typeCol);
        channelTable.getColumns().add(createdCol);
        channelTable.getColumns().add(messagesCol);
        channelTable.getColumns().add(actionsCol);
        
        container.getChildren().addAll(tableTitle, channelTable);
        VBox.setVgrow(channelTable, Priority.ALWAYS);
        
        return container;
    }
    
    private void showCreatePublicChannelDialog() {
        TextInputDialog dialog = new TextInputDialog("My Public Channel");
        dialog.setTitle("Create Public Channel");
        dialog.setHeaderText("Create a public channel for anonymous messaging");
        dialog.setContentText("Channel name:");
        
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                IPChannelConfig config = new IPChannelConfig();
                config.setAllowAnonymous(true);
                config.setRequireAuth(false);
                config.setLogMessages(true);
                
                IPChannel channel = channelService.createChannel(name, config);
                showChannelCreatedDialog(channel);
                refreshChannelList();
            }
        });
    }
    
    private void showCreateTempChannelDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Create Temporary Channel");
        dialog.setHeaderText("Create a temporary channel that expires automatically");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField("Temporary Channel");
        Spinner<Integer> expirySpinner = new Spinner<>(5, 1440, 60, 5); // 5 minutes to 24 hours
        
        grid.add(new Label("Channel name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Expiry (minutes):"), 0, 1);
        grid.add(expirySpinner, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new String[]{nameField.getText(), String.valueOf(expirySpinner.getValue())};
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            String name = result[0];
            int expiry = Integer.parseInt(result[1]);
            
            if (!name.trim().isEmpty()) {
                IPChannel channel = channelService.createTemporaryChannel(name, expiry);
                showChannelCreatedDialog(channel);
                refreshChannelList();
            }
        });
    }
    
    private void showCreateSecureChannelDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Create Secure Channel");
        dialog.setHeaderText("Create a password-protected channel");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField("Secure Channel");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        
        grid.add(new Label("Channel name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new String[]{nameField.getText(), passwordField.getText()};
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            String name = result[0];
            String password = result[1];
            
            if (!name.trim().isEmpty() && !password.trim().isEmpty()) {
                IPChannel channel = channelService.createSecureChannel(name, password);
                showChannelCreatedDialog(channel);
                refreshChannelList();
            }
        });
    }
    
    private void showChannelCreatedDialog(IPChannel channel) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Channel Created Successfully");
        alert.setHeaderText("Your IP channel is ready!");
        
        TextArea urlArea = new TextArea(channel.getUrl());
        urlArea.setEditable(false);
        urlArea.setPrefRowCount(2);
        urlArea.setWrapText(true);
        urlArea.setStyle("-fx-font-family: monospace; -fx-background-color: #f8f9fa;");
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Channel: " + channel.getName()),
            new Label("Share this URL with anyone you want to receive messages from:"),
            urlArea
        );
        
        alert.getDialogPane().setContent(content);
        
        ButtonType copyButton = new ButtonType("📋 Copy URL");
        alert.getButtonTypes().setAll(copyButton, ButtonType.OK);
        
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == copyButton) {
                copyToClipboard(channel.getUrl());
                showNotification("URL copied to clipboard!");
            }
        });
    }
    
    private void deleteChannel(String channelId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Channel");
        confirm.setHeaderText("Are you sure you want to delete this channel?");
        confirm.setContentText("This action cannot be undone. The URL will stop working immediately.");
        
        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (channelService.removeChannel(channelId)) {
                    showNotification("Channel deleted successfully");
                    refreshChannelList();
                } else {
                    showNotification("Failed to delete channel");
                }
            }
        });
    }
    
    private void refreshChannelList() {
        Platform.runLater(() -> {
            channelTable.getItems().clear();
            
            Map<String, IPChannel> channels = channelService.getActiveChannels();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
            
            for (IPChannel channel : channels.values()) {
                String type = "";
                if (channel.getConfig().isTemporary()) {
                    type = channel.isExpired() ? "⏰ Expired" : "⏰ Temporary";
                } else if (channel.getConfig().isRequireAuth()) {
                    type = "🔒 Secure";
                } else {
                    type = "🌐 Public";
                }
                
                ChannelRow row = new ChannelRow(
                    channel.getId(),
                    channel.getName(),
                    channel.getUrl(),
                    type,
                    channel.getCreatedAt().format(formatter),
                    String.valueOf(channel.getMessageCount())
                );
                
                channelTable.getItems().add(row);
            }
            
            statusLabel.setText(String.format("Active channels: %d", channels.size()));
        });
    }
    
    private void copyToClipboard(String text) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        } catch (Exception e) {
            // Fallback for systems without AWT
            System.out.println("URL: " + text);
        }
    }
    
    private void showNotification(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px; -fx-font-weight: bold;");
            
            // Reset style after 3 seconds
            scheduler.schedule(() -> {
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-font-weight: normal;");
                });
            }, 3, TimeUnit.SECONDS);
        });
    }
    
    private void startPeriodicRefresh() {
        // Refresh every 30 seconds
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::refreshChannelList);
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    public void close() {
        if (stage != null) {
            stage.close();
        }
        scheduler.shutdown();
    }
    
    // Data class for table rows
    public static class ChannelRow {
        private final String id;
        private final String name;
        private final String url;
        private final String type;
        private final String created;
        private final String messages;
        
        public ChannelRow(String id, String name, String url, String type, String created, String messages) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.type = type;
            this.created = created;
            this.messages = messages;
        }
        
        // Getters for JavaFX PropertyValueFactory
        public String getId() { return id; }
        public String getName() { return name; }
        public String getUrl() { return url; }
        public String getType() { return type; }
        public String getCreated() { return created; }
        public String getMessages() { return messages; }
    }
}