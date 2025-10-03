package com.p2p.messaging.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.p2p.messaging.model.Message;
import com.p2p.messaging.model.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * IP Channel Service - Creates public URLs that anyone can use to send messages
 * Supports temporary channels, authentication, and security controls
 */
public class IPChannelService {
    
    private static final Logger logger = LoggerFactory.getLogger(IPChannelService.class);
    private static final int CHANNEL_SERVER_PORT = 8095;
    
    private final P2PNetworkManager networkManager;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final Map<String, IPChannel> activeChannels;
    private final AtomicBoolean running;
    
    private ServerSocket channelServer;
    private String publicIP;
    
    public IPChannelService(P2PNetworkManager networkManager) {
        this.networkManager = networkManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.executorService = Executors.newCachedThreadPool();
        this.activeChannels = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(false);
    }
    
    /**
     * Start the IP channel service
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            try {
                channelServer = new ServerSocket(CHANNEL_SERVER_PORT);
                detectPublicIP();
                
                executorService.submit(() -> {
                    logger.info("IP Channel service started on port {}", CHANNEL_SERVER_PORT);
                    logger.info("Public IP detected: {}", publicIP);
                    
                    while (running.get() && !channelServer.isClosed()) {
                        try {
                            Socket clientSocket = channelServer.accept();
                            executorService.submit(() -> handleChannelRequest(clientSocket));
                        } catch (IOException e) {
                            if (running.get()) {
                                logger.error("Error accepting channel connection", e);
                            }
                        }
                    }
                });
                
            } catch (IOException e) {
                logger.error("Failed to start IP channel service", e);
                running.set(false);
            }
        }
    }
    
    /**
     * Create a new IP channel
     */
    public IPChannel createChannel(String channelName, IPChannelConfig config) {
        String channelId = UUID.randomUUID().toString().substring(0, 8);
        String channelUrl = generateChannelURL(channelId);
        
        IPChannel channel = new IPChannel(
            channelId, 
            channelName != null ? channelName : "Anonymous Channel",
            channelUrl,
            config,
            LocalDateTime.now()
        );
        
        activeChannels.put(channelId, channel);
        
        // Create a virtual peer for this channel to open conversation in desktop app
        createChannelPeer(channel);
        
        logger.info("Created IP channel '{}' with URL: {}", channel.getName(), channelUrl);
        return channel;
    }
    
    /**
     * Create a temporary channel (expires after specified minutes)
     */
    public IPChannel createTemporaryChannel(String channelName, int expiryMinutes) {
        IPChannelConfig config = new IPChannelConfig();
        config.setTemporary(true);
        config.setExpiryMinutes(expiryMinutes);
        config.setRequireAuth(false);
        config.setAllowAnonymous(true);
        
        IPChannel channel = createChannel(channelName, config);
        
        // Schedule automatic cleanup
        executorService.submit(() -> {
            try {
                Thread.sleep(expiryMinutes * 60 * 1000);
                removeChannel(channel.getId());
                logger.info("Temporary channel '{}' expired and was removed", channel.getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        return channel;
    }
    
    /**
     * Create a secure channel with authentication
     */
    public IPChannel createSecureChannel(String channelName, String password) {
        IPChannelConfig config = new IPChannelConfig();
        config.setRequireAuth(true);
        config.setPassword(password);
        config.setAllowAnonymous(false);
        config.setLogMessages(true);
        
        return createChannel(channelName, config);
    }
    
    /**
     * Remove a channel
     */
    public boolean removeChannel(String channelId) {
        IPChannel removed = activeChannels.remove(channelId);
        if (removed != null) {
            logger.info("Removed IP channel: {}", removed.getName());
            return true;
        }
        return false;
    }
    
    /**
     * Create a virtual peer for the channel to open conversation in desktop app
     */
    private void createChannelPeer(IPChannel channel) {
        String channelPeerId = "channel_" + channel.getId();
        String displayName = "📺 " + channel.getName() + " (IP Channel)";
        
        // Create a virtual peer representing this channel
        Peer channelPeer = new Peer(
            channelPeerId,
            "IP Channel",
            displayName,
            "channel", // Special IP to indicate it's a channel
            0 // No port for channels
        );
        channelPeer.setStatus(Peer.Status.ONLINE);
        
        // Add the channel peer to network manager to open conversation
        networkManager.handleChannelPeer(channelPeer);
        
        logger.info("Created channel peer for desktop app: {}", displayName);
    }
    
    /**
     * Get all active channels
     */
    public Map<String, IPChannel> getActiveChannels() {
        return new ConcurrentHashMap<>(activeChannels);
    }
    
    /**
     * Handle a message from the desktop app to be stored in IP channel
     */
    public void handleDesktopMessage(Message message) {
        // Find the channel associated with this virtual peer
        String senderId = message.getSenderId();
        if (senderId.startsWith("channel_")) {
            String channelId = extractChannelIdFromPeerId(senderId);
            IPChannel channel = activeChannels.get(channelId);
            
            if (channel != null) {
                // Store the desktop message in the channel
                String senderName = networkManager.getLocalPeer().getDisplayName();
                channel.addMessage(message, senderName, "desktop");
                logger.info("Desktop message added to channel '{}': {}", 
                           channel.getName(), message.getContent());
            }
        }
    }
    
    private String extractChannelIdFromPeerId(String peerId) {
        // Extract channel ID from peer ID format: "channel_<channelId>_<username>"
        String[] parts = peerId.split("_");
        return parts.length > 1 ? parts[1] : null;
    }
    
    private void handleChannelRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String requestLine = in.readLine();
            if (requestLine == null) return;
            
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;
            
            String method = parts[0];
            String path = parts[1];
            
            // Read headers
            Map<String, String> headers = new ConcurrentHashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(": ", 2);
                if (headerParts.length == 2) {
                    headers.put(headerParts[0].toLowerCase(), headerParts[1]);
                }
            }
            
            String clientIP = clientSocket.getRemoteSocketAddress().toString();
            logger.info("Channel request from {}: {} {}", clientIP, method, path);
            
            if ("GET".equals(method)) {
                handleChannelGet(path, out);
            } else if ("POST".equals(method)) {
                logger.info("Handling POST request to: {}", path);
                handleChannelPost(path, headers, in, out, clientIP);
            } else if ("OPTIONS".equals(method)) {
                // Handle CORS preflight requests
                handleCorsOptions(out);
            } else {
                logger.warn("Unsupported method: {}", method);
                serve404(out);
            }
            
        } catch (Exception e) {
            logger.error("Error handling channel request", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.debug("Error closing client socket", e);
            }
        }
    }
    
    private void handleChannelGet(String path, PrintWriter out) {
        try {
            if (path.startsWith("/channel/")) {
                String channelId = extractChannelId(path);
                IPChannel channel = activeChannels.get(channelId);
                
                if (channel != null && !channel.isExpired()) {
                    if (path.endsWith("/messages")) {
                        serveChannelMessages(channel, out);
                    } else if (path.endsWith("/events")) {
                        serveChannelEvents(channel, out);
                    } else if (path.endsWith("/chat")) {
                        serveChannelChatInterface(channel, out);
                    } else {
                        serveChannelInterface(channel, out);
                    }
                } else {
                    serveChannelNotFound(out);
                }
            } else if ("/".equals(path) || "/status".equals(path)) {
                serveChannelStatus(out);
            } else {
                serve404(out);
            }
        } catch (Exception e) {
            logger.error("Error handling channel GET", e);
            serve500(out);
        }
    }
    
    private void handleChannelPost(String path, Map<String, String> headers, 
                                 BufferedReader in, PrintWriter out, String clientIP) {
        try {
            if (path.startsWith("/channel/")) {
                String channelId;
                if (path.endsWith("/send")) {
                    channelId = extractChannelId(path.replace("/send", ""));
                } else if (path.endsWith("/auth")) {
                    channelId = extractChannelId(path.replace("/auth", ""));
                } else {
                    channelId = extractChannelId(path);
                }
                
                IPChannel channel = activeChannels.get(channelId);
                
                if (channel != null && !channel.isExpired()) {
                    // Read POST body
                    int contentLength = Integer.parseInt(headers.getOrDefault("content-length", "0"));
                    char[] body = new char[contentLength];
                    in.read(body, 0, contentLength);
                    String postData = new String(body);
                    
                    if (path.endsWith("/auth")) {
                        logger.debug("Handling auth request for channel {}", channelId);
                        handleChannelAuth(channel, postData, out);
                    } else if (path.endsWith("/send")) {
                        logger.debug("Handling send message request for channel {}", channelId);
                        handleChannelMessage(channel, postData, clientIP, out);
                    }
                } else {
                    serveChannelNotFound(out);
                }
            } else {
                serve404(out);
            }
        } catch (Exception e) {
            logger.error("Error handling channel POST", e);
            serve500(out);
        }
    }
    
    private void handleChannelMessage(IPChannel channel, String postData, String clientIP, PrintWriter out) {
        try {
            logger.debug("Processing message for channel '{}' from {}: {}", 
                        channel.getName(), clientIP, postData);
            // Parse form data or JSON
            Map<String, String> messageData;
            if (postData.startsWith("{")) {
                // JSON format
                @SuppressWarnings("unchecked")
                Map<String, String> jsonData = objectMapper.readValue(postData, Map.class);
                messageData = jsonData;
            } else {
                // Form data format
                messageData = parseFormData(postData);
            }
            
            String senderName = messageData.getOrDefault("sender", "Anonymous");
            String content = messageData.get("message");
            String password = messageData.get("password");
            
            if (content == null || content.trim().isEmpty()) {
                serveError(out, "Message content is required");
                return;
            }
            
            // Check authentication if required
            if (channel.getConfig().isRequireAuth()) {
                if (!channel.getConfig().getPassword().equals(password)) {
                    serveError(out, "Invalid password");
                    return;
                }
            }
            
            // Create virtual peer for the sender
            String senderId = "channel_" + channel.getId() + "_" + senderName.replaceAll("[^a-zA-Z0-9]", "");
            String displayName = senderName + " (via " + channel.getName() + ")";
            
            // Create a virtual peer for this channel sender
            Peer virtualPeer = new Peer(
                senderId,
                senderName,
                displayName,
                "channel", // Special marker to identify channel peers
                0 // No direct port since it's via channel
            );
            virtualPeer.setStatus(Peer.Status.ONLINE);
            
            // Add virtual peer to network manager
            networkManager.handleChannelPeer(virtualPeer);
            
            // Create and send message
            Message message = new Message(senderId, networkManager.getLocalPeer().getId(), content);
            
            // Add channel metadata first
            channel.addMessage(message, senderName, clientIP);
            
            // Forward to P2P network (works even if peers are offline - message will be stored)
            try {
                networkManager.handleExternalMessage(message);
                logger.info("Message forwarded to P2P network: {}", content);
            } catch (Exception e) {
                logger.warn("Failed to forward message to P2P network (peer may be offline): {}", e.getMessage());
                // Continue anyway - the message is still stored in the channel
            }
            
            // Send JSON success response for AJAX
            String timestamp = java.time.LocalDateTime.now().toString();
            String response = String.format(
                "{\"success\":true,\"message\":\"Message sent successfully\",\"timestamp\": \"%s\",\"messageId\":%d}", 
                timestamp, channel.getMessageCount()
            );
            
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json; charset=utf-8");
            out.println("Content-Length: " + response.getBytes("UTF-8").length);
            out.println("Access-Control-Allow-Origin: *");
            out.println("Access-Control-Allow-Methods: POST, GET, OPTIONS");
            out.println("Access-Control-Allow-Headers: Content-Type, Accept");
            out.println("Connection: close");
            out.println();
            out.println(response);
            
            logger.info("Message received via channel '{}' from {}: {}", 
                       channel.getName(), senderName, content);
            
        } catch (Exception e) {
            logger.error("Error handling channel message", e);
            serveError(out, "Failed to process message");
        }
    }
    
    private void handleChannelAuth(IPChannel channel, String postData, PrintWriter out) {
        try {
            // Parse authentication data
            Map<String, String> authData;
            if (postData.startsWith("{")) {
                @SuppressWarnings("unchecked")
                Map<String, String> jsonData = objectMapper.readValue(postData, Map.class);
                authData = jsonData;
            } else {
                authData = parseFormData(postData);
            }
            
            String userName = authData.get("userName");
            String password = authData.get("password");
            
            if (userName == null || userName.trim().isEmpty()) {
                serveAuthError(out, "Name is required");
                return;
            }
            
            // Check password if required
            if (channel.getConfig().isRequireAuth()) {
                if (!channel.getConfig().getPassword().equals(password)) {
                    serveAuthError(out, "Invalid password");
                    return;
                }
            }
            
            // Authentication successful
            String response = "{\"success\":true,\"message\":\"Authentication successful\"}";
            
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json; charset=utf-8");
            out.println("Content-Length: " + response.getBytes().length);
            out.println("Access-Control-Allow-Origin: *");
            out.println("Connection: keep-alive");
            out.println();
            out.println(response);
            
            logger.info("User '{}' authenticated for channel '{}'", userName, channel.getName());
            
        } catch (Exception e) {
            logger.error("Error handling channel authentication", e);
            serveAuthError(out, "Authentication failed");
        }
    }
    
    private void serveAuthError(PrintWriter out, String error) {
        String response = "{\"success\":false,\"message\":\"" + error + "\"}";
        
        out.println("HTTP/1.1 400 Bad Request");
        out.println("Content-Type: application/json; charset=utf-8");
        out.println("Content-Length: " + response.getBytes().length);
        out.println("Access-Control-Allow-Origin: *");
        out.println("Connection: keep-alive");
        out.println();
        out.println(response);
    }
    
    private void serveChannelInterface(IPChannel channel, PrintWriter out) {
        String html = generateChannelLoginHTML(channel);
        
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html; charset=utf-8");
        out.println("Content-Length: " + html.getBytes().length);
        out.println("Connection: keep-alive");
        out.println();
        out.println(html);
    }
    
    private void serveChannelChatInterface(IPChannel channel, PrintWriter out) {
        String html = generateChannelChatHTML(channel);
        
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html; charset=utf-8");
        out.println("Content-Length: " + html.getBytes().length);
        out.println("Connection: keep-alive");
        out.println();
        out.println(html);
    }
    
    private void serveChannelStatus(PrintWriter out) {
        try {
            Map<String, Object> status = Map.of(
                "service", "IP Channel Service",
                "activeChannels", activeChannels.size(),
                "publicIP", publicIP != null ? publicIP : "Unknown",
                "port", CHANNEL_SERVER_PORT,
                "channels", activeChannels.values()
            );
            
            String json = objectMapper.writeValueAsString(status);
            
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + json.getBytes().length);
            out.println("Access-Control-Allow-Origin: *");
            out.println("Connection: close");
            out.println();
            out.println(json);
            
        } catch (Exception e) {
            logger.error("Error serving channel status", e);
            serve500(out);
        }
    }
    
    private void serveChannelMessages(IPChannel channel, PrintWriter out) {
        try {
            // Return recent messages as JSON
            Map<String, Object> response = Map.of(
                "success", true,
                "messages", channel.getRecentMessages(),
                "channelName", channel.getName()
            );
            
            String json = objectMapper.writeValueAsString(response);
            
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json; charset=utf-8");
            out.println("Content-Length: " + json.getBytes().length);
            out.println("Access-Control-Allow-Origin: *");
            out.println("Connection: keep-alive");
            out.println();
            out.println(json);
            
        } catch (Exception e) {
            logger.error("Error serving channel messages", e);
            serve500(out);
        }
    }
    
    private void serveChannelEvents(IPChannel channel, PrintWriter out) {
        // Server-Sent Events for real-time updates
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/event-stream");
        out.println("Cache-Control: no-cache");
        out.println("Connection: keep-alive");
        out.println("Access-Control-Allow-Origin: *");
        out.println();
        out.flush();
        
        try {
            // Send initial data
            out.println("data: " + objectMapper.writeValueAsString(Map.of(
                "type", "connected",
                "channelName", channel.getName()
            )));
            out.println();
            out.flush();
            
            // Keep connection alive for real-time updates
            // In a production system, you'd maintain this connection and push updates
            // For now, we'll close after sending the initial data
            
        } catch (Exception e) {
            logger.error("Error in SSE connection", e);
        }
    }
    
    private void serveChannelNotFound(PrintWriter out) {
        String response = "<html><body><h1>Channel Not Found</h1><p>The requested channel does not exist or has expired.</p></body></html>";
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/html");
        out.println("Content-Length: " + response.getBytes().length);
        out.println("Connection: close");
        out.println();
        out.println(response);
    }
    
    private void serveError(PrintWriter out, String error) {
        String response = "{\"success\":false,\"message\":\"" + error + "\"}";
        out.println("HTTP/1.1 400 Bad Request");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + response.getBytes().length);
        out.println("Connection: close");
        out.println();
        out.println(response);
    }
    
    private void serve404(PrintWriter out) {
        String response = "404 Not Found";
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/plain");
        out.println("Content-Length: " + response.getBytes().length);
        out.println("Connection: close");
        out.println();
        out.println(response);
    }
    
    private void serve500(PrintWriter out) {
        String response = "500 Internal Server Error";
        out.println("HTTP/1.1 500 Internal Server Error");
        out.println("Content-Type: text/plain");
        out.println("Content-Length: " + response.getBytes().length);
        out.println("Connection: close");
        out.println();
        out.println(response);
    }
    
    private void handleCorsOptions(PrintWriter out) {
        out.println("HTTP/1.1 200 OK");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Methods: GET, POST, OPTIONS");
        out.println("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");
        out.println("Access-Control-Max-Age: 86400");
        out.println("Content-Length: 0");
        out.println("Connection: close");
        out.println();
    }
    
    private String generateChannelLoginHTML(IPChannel channel) {
        boolean requiresAuth = channel.getConfig().isRequireAuth();
        
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Join Chat - %s</title>
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        height: 100vh;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        padding: 20px;
                    }
                    
                    .login-container {
                        background: white;
                        border-radius: 20px;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.2);
                        padding: 40px;
                        width: 100%%;
                        max-width: 400px;
                        text-align: center;
                    }
                    
                    .header {
                        margin-bottom: 30px;
                    }
                    
                    .channel-title {
                        font-size: 24px;
                        font-weight: 600;
                        color: #2c3e50;
                        margin-bottom: 8px;
                    }
                    
                    .channel-subtitle {
                        font-size: 16px;
                        color: #7f8c8d;
                        margin-bottom: 15px;
                    }
                    
                    .secure-badge {
                        background: #27ae60;
                        color: white;
                        padding: 5px 15px;
                        border-radius: 20px;
                        font-size: 12px;
                        display: inline-block;
                    }
                    
                    .form-group {
                        margin-bottom: 20px;
                        text-align: left;
                    }
                    
                    .form-group label {
                        display: block;
                        margin-bottom: 8px;
                        font-weight: 600;
                        color: #2c3e50;
                    }
                    
                    .form-input {
                        width: 100%%;
                        padding: 15px;
                        border: 2px solid #e1e5e9;
                        border-radius: 10px;
                        font-size: 16px;
                        outline: none;
                        transition: border-color 0.3s;
                    }
                    
                    .form-input:focus {
                        border-color: #007AFF;
                    }
                    
                    .auth-group {
                        display: %s;
                    }
                    
                    .join-button {
                        background: linear-gradient(45deg, #007AFF, #0056CC);
                        color: white;
                        border: none;
                        border-radius: 10px;
                        padding: 15px 30px;
                        font-size: 16px;
                        font-weight: 600;
                        cursor: pointer;
                        width: 100%%;
                        transition: transform 0.2s;
                    }
                    
                    .join-button:hover {
                        transform: translateY(-2px);
                    }
                    
                    .join-button:disabled {
                        background: #ccc;
                        cursor: not-allowed;
                        transform: none;
                    }
                    
                    .error-message {
                        color: #e74c3c;
                        font-size: 14px;
                        margin-top: 10px;
                        display: none;
                    }
                    
                    .info-text {
                        font-size: 14px;
                        color: #7f8c8d;
                        margin-top: 20px;
                        line-height: 1.4;
                    }
                    
                    @media (max-width: 500px) {
                        .login-container {
                            padding: 30px 20px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="login-container">
                    <div class="header">
                        <div class="channel-title">💬 %s</div>
                        <div class="channel-subtitle">Web Chat</div>
                        %s
                    </div>
                    
                    <form id="loginForm">
                        <div class="form-group">
                            <label for="userName">Your Name</label>
                            <input type="text" id="userName" class="form-input" placeholder="Enter your name..." maxlength="50" required>
                        </div>
                        
                        <div class="form-group auth-group">
                            <label for="channelPassword">Channel Password</label>
                            <input type="password" id="channelPassword" class="form-input" placeholder="Enter password..." required>
                        </div>
                        
                        <button type="submit" class="join-button">Join Chat</button>
                        
                        <div class="error-message" id="errorMessage"></div>
                    </form>
                    
                    <div class="info-text">
                        🔒 This is a secure, direct connection to the channel owner's P2P messaging application.
                    </div>
                </div>
                
                <script>
                    const channelId = '%s';
                    const requiresAuth = %s;
                    
                    const form = document.getElementById('loginForm');
                    const userNameInput = document.getElementById('userName');
                    const passwordInput = document.getElementById('channelPassword');
                    const errorDiv = document.getElementById('errorMessage');
                    
                    form.addEventListener('submit', handleLogin);
                    userNameInput.focus();
                    
                    function handleLogin(e) {
                        e.preventDefault();
                        
                        const userName = userNameInput.value.trim();
                        const password = passwordInput.value;
                        
                        if (!userName) {
                            showError('Please enter your name');
                            return;
                        }
                        
                        if (requiresAuth && !password) {
                            showError('Please enter the channel password');
                            return;
                        }
                        
                        // Authenticate with server
                        const authData = {
                            userName: userName,
                            password: password
                        };
                        
                        fetch('/channel/' + channelId + '/auth', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                            },
                            body: JSON.stringify(authData)
                        })
                        .then(response => response.json())
                        .then(data => {
                            if (data.success) {
                                // Store auth info and redirect to chat
                                sessionStorage.setItem('channelAuth', JSON.stringify({
                                    userName: userName,
                                    password: password,
                                    channelId: channelId
                                }));
                                window.location.href = '/channel/' + channelId + '/chat';
                            } else {
                                showError(data.message || 'Authentication failed');
                            }
                        })
                        .catch(error => {
                            console.error('Auth error:', error);
                            showError('Connection error. Please try again.');
                        });
                    }
                    
                    function showError(message) {
                        errorDiv.textContent = message;
                        errorDiv.style.display = 'block';
                    }
                </script>
            </body>
            </html>
            """, 
            channel.getName(),
            channel.getName(),
            requiresAuth ? "<div class=\"secure-badge\">🔒 SECURE CHANNEL</div>" : "",
            requiresAuth ? "block" : "none",
            channel.getId(),
            requiresAuth ? "true" : "false"
        );
    }
    
    private String generateChannelChatHTML(IPChannel channel) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Chat - %s</title>
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: #f5f7fa;
                        height: 100vh;
                        display: flex;
                        flex-direction: column;
                    }
                    
                    .chat-header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 15px 20px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
                    
                    .chat-title {
                        font-size: 18px;
                        font-weight: 600;
                    }
                    
                    .lock-button {
                        background: rgba(255,255,255,0.2);
                        border: 1px solid rgba(255,255,255,0.3);
                        color: white;
                        padding: 8px 15px;
                        border-radius: 20px;
                        cursor: pointer;
                        font-size: 14px;
                        transition: background 0.2s;
                    }
                    
                    .lock-button:hover {
                        background: rgba(255,255,255,0.3);
                    }
                    
                    .messages-container {
                        flex: 1;
                        overflow-y: auto;
                        padding: 20px;
                        display: flex;
                        flex-direction: column;
                        gap: 15px;
                    }
                    
                    .message {
                        background: white;
                        padding: 15px;
                        border-radius: 15px;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                        max-width: 70%%;
                        word-wrap: break-word;
                    }
                    
                    .message.sent {
                        align-self: flex-end;
                        background: linear-gradient(45deg, #007AFF, #0056CC);
                        color: white;
                    }
                    
                    .message.received {
                        align-self: flex-start;
                        background: white;
                    }
                    
                    .message-sender {
                        font-weight: 600;
                        font-size: 12px;
                        margin-bottom: 5px;
                        opacity: 0.8;
                    }
                    
                    .message-content {
                        font-size: 14px;
                        line-height: 1.4;
                    }
                    
                    .message-time {
                        font-size: 11px;
                        opacity: 0.6;
                        margin-top: 5px;
                    }
                    
                    .input-container {
                        background: white;
                        padding: 20px;
                        box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
                        display: flex;
                        gap: 10px;
                        align-items: flex-end;
                    }
                    
                    .message-input {
                        flex: 1;
                        padding: 12px 15px;
                        border: 2px solid #e1e5e9;
                        border-radius: 25px;
                        font-size: 16px;
                        outline: none;
                        resize: none;
                        max-height: 100px;
                        min-height: 44px;
                        transition: border-color 0.3s;
                    }
                    
                    .message-input:focus {
                        border-color: #007AFF;
                    }
                    
                    .send-button {
                        background: linear-gradient(45deg, #007AFF, #0056CC);
                        color: white;
                        border: none;
                        border-radius: 50%%;
                        width: 44px;
                        height: 44px;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        transition: transform 0.2s;
                    }
                    
                    .send-button:hover {
                        transform: scale(1.05);
                    }
                    
                    .send-button:disabled {
                        background: #ccc;
                        cursor: not-allowed;
                        transform: none;
                    }
                    
                    .lock-overlay {
                        position: fixed;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        background: rgba(0,0,0,0.8);
                        display: none;
                        justify-content: center;
                        align-items: center;
                        z-index: 1000;
                    }
                    
                    .unlock-form {
                        background: white;
                        padding: 40px;
                        border-radius: 20px;
                        text-align: center;
                        max-width: 400px;
                        width: 90%%;
                    }
                    
                    .unlock-title {
                        font-size: 24px;
                        margin-bottom: 20px;
                        color: #2c3e50;
                    }
                    
                    .unlock-input {
                        width: 100%%;
                        padding: 15px;
                        border: 2px solid #e1e5e9;
                        border-radius: 10px;
                        font-size: 16px;
                        margin-bottom: 20px;
                        outline: none;
                    }
                    
                    .unlock-input:focus {
                        border-color: #007AFF;
                    }
                    
                    .unlock-button {
                        background: linear-gradient(45deg, #007AFF, #0056CC);
                        color: white;
                        border: none;
                        border-radius: 10px;
                        padding: 15px 30px;
                        font-size: 16px;
                        font-weight: 600;
                        cursor: pointer;
                        width: 100%%;
                    }
                    
                    .status-indicator {
                        position: fixed;
                        top: 20px;
                        right: 20px;
                        background: #27ae60;
                        color: white;
                        padding: 8px 15px;
                        border-radius: 20px;
                        font-size: 12px;
                        font-weight: 600;
                    }
                    
                    .status-indicator.offline {
                        background: #e74c3c;
                    }
                </style>
            </head>
            <body>
                <div class="status-indicator" id="statusIndicator">🟢 Connected</div>
                
                <div class="chat-header">
                    <div class="chat-title">💬 %s</div>
                    <button class="lock-button" onclick="lockChat()">🔒 Lock Chat</button>
                </div>
                
                <div class="messages-container" id="messagesContainer">
                    <div class="message received">
                        <div class="message-sender">System</div>
                        <div class="message-content">Welcome to the chat! You can start messaging now.</div>
                        <div class="message-time">Just now</div>
                    </div>
                </div>
                
                <div class="input-container">
                    <textarea class="message-input" id="messageInput" placeholder="Type your message... (Enter to send, Shift+Enter for new line)" 
                             rows="1" maxlength="1000"></textarea>
                    <button class="send-button" id="sendButton">➤</button>
                </div>
                
                <div class="lock-overlay" id="lockOverlay">
                    <div class="unlock-form">
                        <div class="unlock-title">🔒 Chat Locked</div>
                        <input type="password" class="unlock-input" id="unlockInput" 
                               placeholder="Enter unlock code...">
                        <button class="unlock-button" onclick="unlockChat()">Unlock Chat</button>
                    </div>
                </div>
                
                <script>
                    const channelId = '%s';
                    let authData = null;
                    let messagePolling = null;
                    let isLocked = false;
                    
                    // Get auth data from session storage
                    (function() {
                        try {
                            authData = JSON.parse(sessionStorage.getItem('channelAuth'));
                            if (!authData || authData.channelId !== channelId) {
                                window.location.href = '/channel/' + channelId;
                                return;
                            }
                        } catch (e) {
                            window.location.href = '/channel/' + channelId;
                            return;
                        }
                    })();
                    
                    const messageInput = document.getElementById('messageInput');
                    const messagesContainer = document.getElementById('messagesContainer');
                    const sendButton = document.getElementById('sendButton');
                    const statusIndicator = document.getElementById('statusIndicator');
                    
                    // Enhanced auto-resize textarea for multi-line messages
                    messageInput.addEventListener('input', function() {
                        // Reset height to auto to calculate new height
                        this.style.height = 'auto';
                        // Set height with better constraints for multi-line messages
                        const maxHeight = 120; // Maximum height (about 6 lines)
                        const minHeight = 40;  // Minimum height (1 line)
                        const newHeight = Math.max(minHeight, Math.min(this.scrollHeight, maxHeight));
                        this.style.height = newHeight + 'px';
                        
                        // If content exceeds max height, enable scrolling
                        if (this.scrollHeight > maxHeight) {
                            this.style.overflowY = 'auto';
                        } else {
                            this.style.overflowY = 'hidden';
                        }
                    });
                    
                    // Enhanced keyboard handling for message sending
                    messageInput.addEventListener('keydown', function(e) {
                        // Send message on Enter or Return (but allow Shift+Enter/Return for new line)
                        if ((e.key === 'Enter' || e.key === 'Return') && !e.shiftKey) {
                            e.preventDefault();
                            console.log('Enter key pressed - sending message');
                            sendMessage();
                        }
                        // Allow Shift+Enter/Return for new lines
                        else if ((e.key === 'Enter' || e.key === 'Return') && e.shiftKey) {
                            // Let the default behavior happen (new line)
                            console.log('Shift+Enter pressed - adding new line');
                        }
                    });
                    
                    // Add click event listener to send button
                    sendButton.addEventListener('click', function(e) {
                        e.preventDefault();
                        console.log('Send button clicked');
                        sendMessage();
                    });
                    
                    let isSending = false; // Guard to prevent double sending
                    
                    function sendMessage() {
                        console.log('Send message function called');
                        
                        // Prevent double sending
                        if (isSending) {
                            console.log('Already sending a message, ignoring duplicate call');
                            return;
                        }
                        
                        if (isLocked) {
                            console.log('Chat is locked, not sending message');
                            alert('Chat is locked. Unlock to send messages.');
                            return;
                        }
                        
                        const message = messageInput.value.trim();
                        console.log('Message to send:', message);
                        
                        if (!message) {
                            console.log('Empty message, not sending');
                            alert('Please enter a message');
                            return;
                        }
                        
                        if (!authData || !authData.userName) {
                            console.error('No auth data found');
                            alert('Authentication error. Please refresh and try again.');
                            return;
                        }
                        
                        isSending = true;
                        sendButton.disabled = true;
                        sendButton.textContent = '...';
                        console.log('Sending message to server...');
                        
                        const messageData = {
                            sender: authData.userName,
                            message: message,
                            password: authData.password || ''
                        };
                        
                        console.log('Sending to URL:', window.location.origin + '/channel/' + channelId + '/send');
                        console.log('Message data:', messageData);
                        
                        // Add a timeout to the fetch request
                        const controller = new AbortController();
                        const timeoutId = setTimeout(() => controller.abort(), 10000); // 10 second timeout
                        
                        fetch('/channel/' + channelId + '/send', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'Accept': 'application/json',
                                'Cache-Control': 'no-cache'
                            },
                            body: JSON.stringify(messageData),
                            signal: controller.signal
                        })
                        .then(response => {
                            clearTimeout(timeoutId);
                            console.log('Server response status:', response.status);
                            console.log('Response headers:', Array.from(response.headers.entries()));
                            
                            if (!response.ok) {
                                throw new Error('HTTP ' + response.status + ': ' + response.statusText);
                            }
                            
                            const contentType = response.headers.get('content-type') || '';
                            if (contentType.includes('application/json')) {
                                return response.json();
                            } else {
                                return response.text().then(text => {
                                    console.error('Expected JSON but got:', text);
                                    throw new Error('Server returned non-JSON response: ' + text);
                                });
                            }
                        })
                        .then(data => {
                            console.log('Server response data:', data);
                            
                            if (data && data.success) {
                                addMessage(authData.userName, message, new Date(), true);
                                messageInput.value = '';
                                messageInput.style.height = 'auto';
                                
                                // Update lastMessageId to prevent polling from adding duplicate
                                if (data.messageId) {
                                    lastMessageId = Math.max(lastMessageId, data.messageId);
                                    console.log('Updated lastMessageId to:', lastMessageId);
                                }
                                
                                console.log('Message sent successfully');
                            } else {
                                const errorMsg = (data && data.message) || 'Unknown error';
                                console.error('Server error:', errorMsg);
                                alert('Failed to send message: ' + errorMsg);
                            }
                        })
                        .catch(error => {
                            clearTimeout(timeoutId);
                            console.error('Send error:', error);
                            if (error.name === 'AbortError') {
                                alert('Request timed out. Please check your connection and try again.');
                            } else {
                                alert('Failed to send message: ' + error.message);
                            }
                        })
                        .finally(() => {
                            isSending = false;
                            sendButton.disabled = false;
                            sendButton.textContent = '➤';
                        });
                    }
                    
                    function addMessage(sender, content, timestamp, isSent = false) {
                        const messageDiv = document.createElement('div');
                        messageDiv.className = 'message ' + (isSent ? 'sent' : 'received');
                        
                        messageDiv.innerHTML = 
                            '<div class="message-sender">' + escapeHtml(sender) + '</div>' +
                            '<div class="message-content">' + escapeHtml(content) + '</div>' +
                            '<div class="message-time">' + formatTime(timestamp) + '</div>';
                        
                        messagesContainer.appendChild(messageDiv);
                        messagesContainer.scrollTop = messagesContainer.scrollHeight;
                    }
                    
                    function escapeHtml(text) {
                        const div = document.createElement('div');
                        div.textContent = text;
                        return div.innerHTML;
                    }
                    
                    function formatTime(timestamp) {
                        const date = new Date(timestamp);
                        return date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
                    }
                    
                    function lockChat() {
                        isLocked = true;
                        document.getElementById('lockOverlay').style.display = 'flex';
                        document.getElementById('unlockInput').focus();
                    }
                    
                    function unlockChat() {
                        const code = document.getElementById('unlockInput').value;
                        if (code === authData.password) {
                            isLocked = false;
                            document.getElementById('lockOverlay').style.display = 'none';
                            document.getElementById('unlockInput').value = '';
                        } else {
                            alert('Invalid unlock code');
                        }
                    }
                    
                    let lastMessageId = 0; // Track last displayed message
                    
                    // Poll for new messages every 2 seconds
                    function startMessagePolling() {
                        messagePolling = setInterval(() => {
                            if (isLocked) return;
                            
                            fetch('/channel/' + channelId + '/messages')
                                .then(response => response.json())
                                .then(data => {
                                    if (data.success && data.messages) {
                                        console.log('Retrieved messages:', data.messages);
                                        
                                        // Display new messages that haven't been shown yet
                                        data.messages.forEach(msg => {
                                            if (msg.id > lastMessageId) {
                                                const isOwnMessage = msg.sender === authData.userName;
                                                console.log('Adding polled message:', msg.id, 'from', msg.sender, '(own:', isOwnMessage, ')');
                                                addMessage(msg.sender, msg.content, msg.timestamp, isOwnMessage);
                                                lastMessageId = Math.max(lastMessageId, msg.id);
                                            } else {
                                                console.log('Skipping duplicate message:', msg.id, '(lastId:', lastMessageId, ')');
                                            }
                                        });
                                        
                                        statusIndicator.textContent = '🟢 Connected';
                                        statusIndicator.className = 'status-indicator';
                                    }
                                })
                                .catch(error => {
                                    console.error('Polling error:', error);
                                    statusIndicator.textContent = '🔴 Disconnected';
                                    statusIndicator.className = 'status-indicator offline';
                                });
                        }, 2000);
                    }
                    
                    // Start polling for messages
                    startMessagePolling();
                    
                    // Focus message input
                    messageInput.focus();
                    
                    // Unlock overlay Enter key handling
                    document.getElementById('unlockInput').addEventListener('keydown', function(e) {
                        if (e.key === 'Enter') {
                            unlockChat();
                        }
                    });
                </script>
            </body>
            </html>
            """,
            channel.getName(),
            channel.getName(),
            channel.getId()
        );
    }
    
    private String extractChannelId(String path) {
        String[] parts = path.split("/");
        return parts.length > 2 ? parts[2] : null;
    }
    
    private Map<String, String> parseFormData(String data) {
        Map<String, String> result = new ConcurrentHashMap<>();
        if (data != null && !data.isEmpty()) {
            String[] pairs = data.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], "UTF-8");
                        String value = URLDecoder.decode(keyValue[1], "UTF-8");
                        result.put(key, value);
                    } catch (UnsupportedEncodingException e) {
                        logger.error("Error decoding form data", e);
                    }
                }
            }
        }
        return result;
    }
    
    private void detectPublicIP() {
        try {
            // Try to detect public IP (for demonstration - in real use, you might use a service)
            String localIP = networkManager.getLocalPeer().getIpAddress();
            
            // For now, use local IP (in production, you'd detect actual public IP)
            this.publicIP = localIP;
            
            // You could use a service like: httpbin.org/ip, icanhazip.com, etc.
            // to get the real public IP if needed
            
        } catch (Exception e) {
            logger.warn("Could not detect public IP", e);
            this.publicIP = "localhost";
        }
    }
    
    private String generateChannelURL(String channelId) {
        String host = publicIP != null ? publicIP : "localhost";
        return String.format("http://%s:%d/channel/%s", host, CHANNEL_SERVER_PORT, channelId);
    }
    
    private String extractIPFromClient(String clientAddress) {
        // clientAddress format is typically "/127.0.0.1:12345"
        if (clientAddress.startsWith("/")) {
            clientAddress = clientAddress.substring(1);
        }
        int colonIndex = clientAddress.indexOf(':');
        if (colonIndex > 0) {
            return clientAddress.substring(0, colonIndex);
        }
        return clientAddress;
    }
    
    /**
     * Stop the IP channel service
     */
    public void stop() {
        running.set(false);
        
        try {
            if (channelServer != null) {
                channelServer.close();
            }
        } catch (IOException e) {
            logger.error("Error stopping IP channel service", e);
        }
        
        executorService.shutdown();
        activeChannels.clear();
        
        logger.info("IP Channel service stopped");
    }
    
    // Inner classes for channel management
    
    public static class IPChannel {
        private final String id;
        private final String name;
        private final String url;
        private final IPChannelConfig config;
        private final LocalDateTime createdAt;
        private int messageCount = 0;
        private final List<ChannelMessage> messages = new ArrayList<>();
        
        public IPChannel(String id, String name, String url, IPChannelConfig config, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.config = config;
            this.createdAt = createdAt;
        }
        
        public boolean isExpired() {
            if (config.isTemporary() && config.getExpiryMinutes() > 0) {
                return createdAt.plusMinutes(config.getExpiryMinutes()).isBefore(LocalDateTime.now());
            }
            return false;
        }
        
        public void addMessage(Message message, String senderName, String clientIP) {
            messageCount++;
            
            // Store the message for the web chat
            ChannelMessage channelMessage = new ChannelMessage(
                messageCount, senderName, message.getContent(), LocalDateTime.now(), clientIP
            );
            messages.add(channelMessage);
            
            // Keep only last 100 messages to prevent memory issues
            if (messages.size() > 100) {
                messages.remove(0);
            }
            
            if (config.isLogMessages()) {
                System.out.printf("[Channel: %s] Message #%d from %s (%s): %s%n", 
                                name, messageCount, senderName, clientIP, message.getContent());
            }
        }
        
        public List<ChannelMessage> getRecentMessages() {
            return new ArrayList<>(messages);
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getUrl() { return url; }
        public IPChannelConfig getConfig() { return config; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public int getMessageCount() { return messageCount; }
    }
    
    public static class IPChannelConfig {
        private boolean requireAuth = false;
        private String password;
        private boolean allowAnonymous = true;
        private boolean temporary = false;
        private int expiryMinutes = 0;
        private boolean logMessages = true;
        
        // Getters and setters
        public boolean isRequireAuth() { return requireAuth; }
        public void setRequireAuth(boolean requireAuth) { this.requireAuth = requireAuth; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public boolean isAllowAnonymous() { return allowAnonymous; }
        public void setAllowAnonymous(boolean allowAnonymous) { this.allowAnonymous = allowAnonymous; }
        
        public boolean isTemporary() { return temporary; }
        public void setTemporary(boolean temporary) { this.temporary = temporary; }
        
        public int getExpiryMinutes() { return expiryMinutes; }
        public void setExpiryMinutes(int expiryMinutes) { this.expiryMinutes = expiryMinutes; }
        
        public boolean isLogMessages() { return logMessages; }
        public void setLogMessages(boolean logMessages) { this.logMessages = logMessages; }
    }
    
    public static class ChannelMessage {
        private final int id;
        private final String sender;
        private final String content;
        private final LocalDateTime timestamp;
        private final String clientIP;
        
        public ChannelMessage(int id, String sender, String content, LocalDateTime timestamp, String clientIP) {
            this.id = id;
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.clientIP = clientIP;
        }
        
        // Getters for Jackson serialization
        public int getId() { return id; }
        public String getSender() { return sender; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getClientIP() { return clientIP; }
    }
}