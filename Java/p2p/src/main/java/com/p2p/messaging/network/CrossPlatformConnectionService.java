package com.p2p.messaging.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.p2p.messaging.model.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced connection service for connecting to various platforms
 * Supports Android servers, iOS devices, web servers, and other P2P clients
 */
public class CrossPlatformConnectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CrossPlatformConnectionService.class);
    
    // Common ports for different platforms
    private static final int[] COMMON_P2P_PORTS = {8080, 8081, 9090, 3000, 5000, 8000};
    private static final int[] ANDROID_COMMON_PORTS = {8080, 8888, 9999, 7777};
    private static final int[] WEB_SERVER_PORTS = {80, 443, 8080, 3000, 8000};
    
    private final P2PNetworkManager networkManager;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;
    
    public CrossPlatformConnectionService(P2PNetworkManager networkManager) {
        this.networkManager = networkManager;
        this.executorService = Executors.newCachedThreadPool();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    /**
     * Scan and connect to Android devices on the network
     */
    public CompletableFuture<List<Peer>> scanForAndroidDevices() {
        return CompletableFuture.supplyAsync(() -> {
            List<Peer> androidPeers = new ArrayList<>();
            
            try {
                String networkBase = getNetworkBase();
                logger.info("Scanning for Android devices on network: {}", networkBase);
                
                List<CompletableFuture<Void>> scanTasks = new ArrayList<>();
                
                // Scan common Android IP range (192.168.x.x)
                for (int i = 1; i <= 254; i++) {
                    String targetIP = networkBase + i;
                    
                    CompletableFuture<Void> scanTask = CompletableFuture.runAsync(() -> {
                        for (int port : ANDROID_COMMON_PORTS) {
                            if (isPortOpen(targetIP, port, 1000)) {
                                Peer androidPeer = createAndroidPeer(targetIP, port);
                                if (androidPeer != null) {
                                    androidPeers.add(androidPeer);
                                    logger.info("Found Android device: {}:{}", targetIP, port);
                                }
                                break; // Found one port, move to next IP
                            }
                        }
                    }, executorService);
                    
                    scanTasks.add(scanTask);
                }
                
                // Wait for all scans to complete
                CompletableFuture.allOf(scanTasks.toArray(new CompletableFuture[0])).join();
                
            } catch (Exception e) {
                logger.error("Error scanning for Android devices", e);
            }
            
            return androidPeers;
        }, executorService);
    }
    
    /**
     * Connect to a specific server (Android, web server, etc.)
     */
    public CompletableFuture<Boolean> connectToServer(String address, int port, String serverType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Attempting to connect to {} server at {}:{}", serverType, address, port);
                
                // First check if the server is reachable
                if (!isPortOpen(address, port, 5000)) {
                    logger.warn("Server at {}:{} is not reachable", address, port);
                    return false;
                }
                
                // Attempt different connection protocols
                boolean connected = false;
                
                // Try P2P protocol first
                if (tryP2PConnection(address, port)) {
                    connected = true;
                } else if (tryHttpConnection(address, port)) {
                    // Try HTTP-based connection for web servers
                    connected = true;
                } else if (trySocketConnection(address, port)) {
                    // Try raw socket connection
                    connected = true;
                }
                
                if (connected) {
                    logger.info("Successfully connected to {} server at {}:{}", serverType, address, port);
                } else {
                    logger.warn("Failed to establish connection to {} server at {}:{}", serverType, address, port);
                }
                
                return connected;
                
            } catch (Exception e) {
                logger.error("Error connecting to server {}:{}", address, port, e);
                return false;
            }
        }, executorService);
    }
    
    /**
     * Auto-discover and connect to various platform types
     */
    public CompletableFuture<List<ConnectionInfo>> discoverAllPlatforms() {
        return CompletableFuture.supplyAsync(() -> {
            List<ConnectionInfo> discoveries = new ArrayList<>();
            
            try {
                String networkBase = getNetworkBase();
                logger.info("Discovering all platforms on network: {}", networkBase);
                
                List<CompletableFuture<Void>> discoveryTasks = new ArrayList<>();
                
                for (int i = 1; i <= 254; i++) {
                    String targetIP = networkBase + i;
                    
                    CompletableFuture<Void> discoveryTask = CompletableFuture.runAsync(() -> {
                        // Check for P2P clients
                        for (int port : COMMON_P2P_PORTS) {
                            if (isPortOpen(targetIP, port, 1000)) {
                                String platformType = detectPlatformType(targetIP, port);
                                discoveries.add(new ConnectionInfo(targetIP, port, platformType));
                                break;
                            }
                        }
                    }, executorService);
                    
                    discoveryTasks.add(discoveryTask);
                }
                
                CompletableFuture.allOf(discoveryTasks.toArray(new CompletableFuture[0])).join();
                
            } catch (Exception e) {
                logger.error("Error during platform discovery", e);
            }
            
            return discoveries;
        }, executorService);
    }
    
    /**
     * Connect using QR code (for Android app integration)
     */
    public boolean connectViaQRCode(String qrData) {
        try {
            // Parse QR code data (expected format: "p2p://ip:port" or JSON)
            if (qrData.startsWith("p2p://")) {
                String connectionString = qrData.substring(6);
                String[] parts = connectionString.split(":");
                if (parts.length == 2) {
                    String ip = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    networkManager.connectToPeer(ip, port);
                    return true;
                }
            } else {
                // Try to parse as JSON
                ConnectionInfo info = objectMapper.readValue(qrData, ConnectionInfo.class);
                networkManager.connectToPeer(info.getAddress(), info.getPort());
                return true;
            }
        } catch (Exception e) {
            logger.error("Error connecting via QR code: {}", qrData, e);
        }
        return false;
    }
    
    /**
     * Generate QR code data for this peer (for Android apps to scan)
     */
    public String generateQRCodeData() {
        try {
            Peer localPeer = networkManager.getLocalPeer();
            ConnectionInfo info = new ConnectionInfo(
                localPeer.getIpAddress(), 
                localPeer.getPort(), 
                "Java-P2P"
            );
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            logger.error("Error generating QR code data", e);
            return null;
        }
    }
    
    private boolean isPortOpen(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private String getNetworkBase() throws Exception {
        String localIP = InetAddress.getLocalHost().getHostAddress();
        String[] parts = localIP.split("\\.");
        return parts[0] + "." + parts[1] + "." + parts[2] + ".";
    }
    
    private Peer createAndroidPeer(String ip, int port) {
        try {
            String peerId = "android_" + ip.replace(".", "_") + "_" + port;
            return new Peer(peerId, "Android Device", "Android (" + ip + ")", ip, port);
        } catch (Exception e) {
            logger.error("Error creating Android peer", e);
            return null;
        }
    }
    
    private boolean tryP2PConnection(String address, int port) {
        try {
            networkManager.connectToPeer(address, port);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean tryHttpConnection(String address, int port) {
        try {
            @SuppressWarnings("deprecation")
            URL url = new URL("http://" + address + ":" + port + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int responseCode = conn.getResponseCode();
            return responseCode >= 200 && responseCode < 400;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean trySocketConnection(String address, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, port), 3000);
            return socket.isConnected();
        } catch (Exception e) {
            return false;
        }
    }
    
    private String detectPlatformType(String ip, int port) {
        // Try to detect platform type based on response
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 2000);
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println("PLATFORM_CHECK");
            socket.setSoTimeout(2000);
            String response = in.readLine();
            
            if (response != null) {
                if (response.contains("android")) return "Android";
                if (response.contains("ios")) return "iOS";
                if (response.contains("web")) return "Web Server";
                if (response.contains("java")) return "Java P2P";
            }
            
        } catch (Exception e) {
            // Silent fail, will return Unknown
        }
        
        // Default detection based on common ports
        if (port == 8080 || port == 8888) return "Likely Android";
        if (port == 80 || port == 443) return "Web Server";
        if (port == 3000) return "Node.js Server";
        
        return "Unknown Platform";
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
    
    /**
     * Connection information for discovered services
     */
    public static class ConnectionInfo {
        private String address;
        private int port;
        private String platformType;
        private String displayName;
        
        public ConnectionInfo() {}
        
        public ConnectionInfo(String address, int port, String platformType) {
            this.address = address;
            this.port = port;
            this.platformType = platformType;
            this.displayName = platformType + " (" + address + ":" + port + ")";
        }
        
        // Getters and setters
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getPlatformType() { return platformType; }
        public void setPlatformType(String platformType) { this.platformType = platformType; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }
}