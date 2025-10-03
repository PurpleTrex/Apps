package com.p2p.messaging.ui;

import com.p2p.messaging.model.Message;
import com.p2p.messaging.model.Peer;
import com.p2p.messaging.network.P2PNetworkManager;
import com.p2p.messaging.network.CrossPlatformConnectionService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Modern main window for the P2P messaging application
 * Features a clean, responsive design with support for multiple file types
 */
public class MainWindow {
    
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
    
    private final P2PNetworkManager networkManager;
    private final CrossPlatformConnectionService connectionService;
    private final Map<String, ChatPane> chatPanes;
    private IPChannelManager ipChannelManager;
    
    // UI Components
    private BorderPane root;
    private ListView<Peer> peerList;
    private TabPane chatTabPane;
    private Label statusLabel;
    
    public MainWindow(P2PNetworkManager networkManager) {
        this.networkManager = networkManager;
        this.connectionService = new CrossPlatformConnectionService(networkManager);
        this.chatPanes = new HashMap<>();
        
        // Setup network event listeners
        networkManager.addMessageListener(this::onMessageReceived);
        networkManager.addPeerListener(this::onPeerConnected);
    }
    
    public void show(Stage stage) {
        createUI();
        setupEventHandlers();
        loadInitialData();
        
        stage.setTitle("P2P Messaging - " + networkManager.getLocalPeer().getDisplayName());
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
        
        logger.info("Main window displayed");
    }
    
    private void createUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Create top toolbar
        ToolBar toolbar = createToolbar();
        root.setTop(toolbar);
        
        // Create left sidebar with peer list
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);
        
        // Create center chat area
        chatTabPane = new TabPane();
        chatTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        root.setCenter(chatTabPane);
        
        // Create bottom status bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);
    }
    
    private ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: #2c3e50;");
        
        // App title
        Label titleLabel = new Label("P2P Messaging Service");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);
        
        // Connect button
        Button connectBtn = new Button("Connect to Peer");
        connectBtn.setOnAction(e -> showConnectDialog());
        
        // Android scan button
        Button androidBtn = new Button("Scan Android Devices");
        androidBtn.setOnAction(e -> scanForAndroidDevices());
        
        // Platform discovery button
        Button discoverBtn = new Button("Discover All Platforms");
        discoverBtn.setOnAction(e -> discoverAllPlatforms());
        
        // IP Channel Manager button
        Button channelBtn = new Button("📡 IP Channels");
        channelBtn.setOnAction(e -> showIPChannelManager());
        
        // Settings button
        Button settingsBtn = new Button("Settings");
        settingsBtn.setOnAction(e -> showSettingsDialog());
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Status indicator
        Label statusIndicator = new Label("🟢 Online");
        statusIndicator.setTextFill(Color.WHITE);
        
        toolbar.getItems().addAll(titleLabel, new Separator(), connectBtn, androidBtn, discoverBtn, channelBtn, settingsBtn, spacer, statusIndicator);
        return toolbar;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #34495e;");
        
        // Sidebar title
        Label peersLabel = new Label("Connected Peers");
        peersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        peersLabel.setTextFill(Color.WHITE);
        
        // Peer list
        peerList = new ListView<>();
        peerList.setCellFactory(listView -> new PeerListCell());
        peerList.setStyle("-fx-background-color: #34495e; -fx-border-color: transparent;");
        
        // Local peer info
        VBox localPeerInfo = createLocalPeerInfo();
        
        sidebar.getChildren().addAll(peersLabel, peerList, localPeerInfo);
        VBox.setVgrow(peerList, Priority.ALWAYS);
        
        return sidebar;
    }
    
    private VBox createLocalPeerInfo() {
        VBox localInfo = new VBox(5);
        localInfo.setStyle("-fx-background-color: #2c3e50; -fx-padding: 10; -fx-background-radius: 5;");
        
        Peer localPeer = networkManager.getLocalPeer();
        
        Label nameLabel = new Label(localPeer.getDisplayName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nameLabel.setTextFill(Color.WHITE);
        
        Label addressLabel = new Label(localPeer.getConnectionAddress());
        addressLabel.setFont(Font.font("Arial", 10));
        addressLabel.setTextFill(Color.LIGHTGRAY);
        
        localInfo.getChildren().addAll(nameLabel, addressLabel);
        return localInfo;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 15, 5, 15));
        statusBar.setStyle("-fx-background-color: #ecf0f1;");
        
        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("Arial", 10));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label versionLabel = new Label("v1.0.0");
        versionLabel.setFont(Font.font("Arial", 10));
        versionLabel.setTextFill(Color.GRAY);
        
        statusBar.getChildren().addAll(statusLabel, spacer, versionLabel);
        return statusBar;
    }
    
    private void setupEventHandlers() {
        peerList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Peer selectedPeer = peerList.getSelectionModel().getSelectedItem();
                if (selectedPeer != null) {
                    openChatWithPeer(selectedPeer);
                }
            }
        });
    }
    
    private void loadInitialData() {
        updatePeerList();
        updateStatus("Application started");
    }
    
    private void openChatWithPeer(Peer peer) {
        String tabId = peer.getId();
        
        // Check if chat is already open
        for (Tab tab : chatTabPane.getTabs()) {
            if (tabId.equals(tab.getId())) {
                chatTabPane.getSelectionModel().select(tab);
                return;
            }
        }
        
        // Create new chat pane
        ChatPane chatPane = new ChatPane(peer, networkManager);
        chatPanes.put(tabId, chatPane);
        
        Tab chatTab = new Tab(peer.getDisplayName(), chatPane.getRoot());
        chatTab.setId(tabId);
        
        // Handle tab closing
        chatTab.setOnClosed(e -> chatPanes.remove(tabId));
        
        chatTabPane.getTabs().add(chatTab);
        chatTabPane.getSelectionModel().select(chatTab);
        
        logger.debug("Opened chat with peer: {}", peer.getDisplayName());
    }
    
    private void showConnectDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Connect to Peer");
        dialog.setHeaderText("Enter peer connection details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField ipField = new TextField();
        ipField.setPromptText("IP Address");
        TextField portField = new TextField("8080");
        portField.setPromptText("Port");
        
        grid.add(new Label("IP Address:"), 0, 0);
        grid.add(ipField, 1, 0);
        grid.add(new Label("Port:"), 0, 1);
        grid.add(portField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new String[]{ipField.getText(), portField.getText()};
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            try {
                String ip = result[0].trim();
                int port = Integer.parseInt(result[1].trim());
                
                if (!ip.isEmpty()) {
                    networkManager.connectToPeer(ip, port);
                    updateStatus("Connecting to " + ip + ":" + port + "...");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid port number");
            }
        });
    }
    
    private void scanForAndroidDevices() {
        updateStatus("Scanning for Android devices...");
        
        connectionService.scanForAndroidDevices().thenAccept(androidPeers -> {
            Platform.runLater(() -> {
                if (androidPeers.isEmpty()) {
                    showAlert("No Android devices found on the network.");
                    updateStatus("Android scan complete - no devices found");
                } else {
                    showAndroidDevicesDialog(androidPeers);
                    updateStatus("Found " + androidPeers.size() + " Android device(s)");
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                showAlert("Error scanning for Android devices: " + throwable.getMessage());
                updateStatus("Android scan failed");
            });
            return null;
        });
    }
    
    private void discoverAllPlatforms() {
        updateStatus("Discovering all platforms...");
        
        connectionService.discoverAllPlatforms().thenAccept(connections -> {
            Platform.runLater(() -> {
                if (connections.isEmpty()) {
                    showAlert("No platforms found on the network.");
                    updateStatus("Platform discovery complete - no platforms found");
                } else {
                    showPlatformDiscoveryDialog(connections);
                    updateStatus("Found " + connections.size() + " platform(s)");
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                showAlert("Error during platform discovery: " + throwable.getMessage());
                updateStatus("Platform discovery failed");
            });
            return null;
        });
    }
    
    private void showAndroidDevicesDialog(java.util.List<Peer> androidPeers) {
        Dialog<Peer> dialog = new Dialog<>();
        dialog.setTitle("Android Devices Found");
        dialog.setHeaderText("Select an Android device to connect to:");
        
        ListView<Peer> deviceList = new ListView<>();
        deviceList.getItems().addAll(androidPeers);
        deviceList.setCellFactory(listView -> new ListCell<Peer>() {
            @Override
            protected void updateItem(Peer peer, boolean empty) {
                super.updateItem(peer, empty);
                if (empty || peer == null) {
                    setText(null);
                } else {
                    setText(peer.getDisplayName() + " - " + peer.getConnectionAddress());
                }
            }
        });
        
        dialog.getDialogPane().setContent(deviceList);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return deviceList.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(selectedPeer -> {
            networkManager.connectToPeer(selectedPeer.getIpAddress(), selectedPeer.getPort());
            updateStatus("Connecting to Android device: " + selectedPeer.getDisplayName());
        });
    }
    
    private void showPlatformDiscoveryDialog(java.util.List<CrossPlatformConnectionService.ConnectionInfo> connections) {
        Dialog<CrossPlatformConnectionService.ConnectionInfo> dialog = new Dialog<>();
        dialog.setTitle("Platforms Discovered");
        dialog.setHeaderText("Select a platform to connect to:");
        
        ListView<CrossPlatformConnectionService.ConnectionInfo> platformList = new ListView<>();
        platformList.getItems().addAll(connections);
        platformList.setCellFactory(listView -> new ListCell<CrossPlatformConnectionService.ConnectionInfo>() {
            @Override
            protected void updateItem(CrossPlatformConnectionService.ConnectionInfo info, boolean empty) {
                super.updateItem(info, empty);
                if (empty || info == null) {
                    setText(null);
                } else {
                    setText(info.getDisplayName());
                }
            }
        });
        
        dialog.getDialogPane().setContent(platformList);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return platformList.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(selectedInfo -> {
            connectionService.connectToServer(
                selectedInfo.getAddress(), 
                selectedInfo.getPort(), 
                selectedInfo.getPlatformType()
            ).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        updateStatus("Connected to " + selectedInfo.getPlatformType());
                    } else {
                        showAlert("Failed to connect to " + selectedInfo.getDisplayName());
                    }
                });
            });
        });
    }
    
    private void showSettingsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Settings & Connection Info");
        dialog.setHeaderText("P2P Messaging Settings");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Connection info
        Label infoLabel = new Label("Your Connection Information:");
        infoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Peer localPeer = networkManager.getLocalPeer();
        Label addressLabel = new Label("IP Address: " + localPeer.getIpAddress());
        Label portLabel = new Label("Port: " + localPeer.getPort());
        Label idLabel = new Label("Peer ID: " + localPeer.getId());
        
        // Mobile web interface info
        Label mobileLabel = new Label("Mobile Web Interface:");
        mobileLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        String mobileURL = networkManager.getMobileWebURL();
        TextField mobileUrlField = new TextField(mobileURL != null ? mobileURL : "Starting...");
        mobileUrlField.setEditable(false);
        
        Label mobileInstructions = new Label("📱 Open this URL on any mobile device browser to connect:");
        
        // QR Code info
        Label qrLabel = new Label("QR Code Data (for Android apps):");
        qrLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        String qrData = connectionService.generateQRCodeData();
        TextArea qrArea = new TextArea(qrData);
        qrArea.setEditable(false);
        qrArea.setPrefRowCount(3);
        
        // Instructions
        Label instructionsLabel = new Label("Connection Instructions:");
        instructionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        String instructions = 
            "• Android Devices: Use 'Scan Android Devices' button\n" +
            "• All Platforms: Use 'Discover All Platforms' button\n" +
            "• Manual Connection: Use 'Connect to Peer' with IP:Port\n" +
            "• QR Code: Share the QR data above with Android apps\n" +
            "• Automatic: Peers on same network are auto-discovered";
        
        Label instructionsText = new Label(instructions);
        instructionsText.setWrapText(true);
        
        content.getChildren().addAll(
            infoLabel, addressLabel, portLabel, idLabel,
            new Separator(),
            mobileLabel, mobileInstructions, mobileUrlField,
            new Separator(),
            qrLabel, qrArea,
            new Separator(),
            instructionsLabel, instructionsText
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }
    
    private void showIPChannelManager() {
        if (ipChannelManager == null) {
            ipChannelManager = new IPChannelManager(networkManager.getIPChannelService());
        }
        ipChannelManager.show();
    }
    
    private void onMessageReceived(Message message) {
        Platform.runLater(() -> {
            // Update chat pane if open
            ChatPane chatPane = chatPanes.get(message.getSenderId());
            if (chatPane != null) {
                chatPane.addMessage(message);
            }
            
            updateStatus("Message received from " + message.getSenderId());
        });
    }
    
    private void onPeerConnected(Peer peer) {
        Platform.runLater(() -> {
            updatePeerList();
            updateStatus("Peer connected: " + peer.getDisplayName());
        });
    }
    
    private void updatePeerList() {
        Collection<Peer> peers = networkManager.getConnectedPeers();
        Platform.runLater(() -> {
            peerList.getItems().clear();
            peerList.getItems().addAll(peers);
        });
    }
    
    private void updateStatus(String status) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(status);
            }
        });
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void cleanup() {
        // Cleanup resources
        for (ChatPane chatPane : chatPanes.values()) {
            chatPane.cleanup();
        }
        chatPanes.clear();
        
        if (connectionService != null) {
            connectionService.shutdown();
        }
    }
    
    // Custom list cell for peers
    private static class PeerListCell extends ListCell<Peer> {
        @Override
        protected void updateItem(Peer peer, boolean empty) {
            super.updateItem(peer, empty);
            
            if (empty || peer == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox hbox = new HBox(10);
                hbox.setAlignment(Pos.CENTER_LEFT);
                
                Label statusIcon = new Label(peer.getStatusIcon());
                Label nameLabel = new Label(peer.getDisplayName());
                nameLabel.setTextFill(Color.WHITE);
                nameLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
                
                hbox.getChildren().addAll(statusIcon, nameLabel);
                setGraphic(hbox);
            }
        }
    }
}