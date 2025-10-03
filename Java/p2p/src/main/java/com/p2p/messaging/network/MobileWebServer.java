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
import java.util.Map;

/**
 * Simple HTTP server for mobile web interface
 * Allows mobile devices to connect via browser without installing apps
 */
public class MobileWebServer {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileWebServer.class);
    private static final int WEB_SERVER_PORT = 8090;
    
    private final P2PNetworkManager networkManager;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final Map<String, MobileClient> mobileClients;
    
    private ServerSocket serverSocket;
    private volatile boolean running;
    
    public MobileWebServer(P2PNetworkManager networkManager) {
        this.networkManager = networkManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.executorService = Executors.newCachedThreadPool();
        this.mobileClients = new ConcurrentHashMap<>();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(WEB_SERVER_PORT);
            running = true;
            
            executorService.submit(() -> {
                logger.info("Mobile web server started on port {}", WEB_SERVER_PORT);
                
                while (running && !serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        executorService.submit(() -> handleWebRequest(clientSocket));
                    } catch (IOException e) {
                        if (running) {
                            logger.error("Error accepting web client connection", e);
                        }
                    }
                }
            });
            
        } catch (IOException e) {
            logger.error("Failed to start mobile web server", e);
        }
    }
    
    private void handleWebRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String requestLine = in.readLine();
            if (requestLine == null) return;
            
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;
            
            String method = parts[0];
            String path = parts[1];
            
            // Read headers
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                // Skip headers for simple implementation
            }
            
            logger.debug("Web request: {} {}", method, path);
            
            if ("GET".equals(method)) {
                handleGetRequest(path, out);
            } else if ("POST".equals(method)) {
                handlePostRequest(path, in, out);
            }
            
        } catch (Exception e) {
            logger.error("Error handling web request", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.debug("Error closing client socket", e);
            }
        }
    }
    
    private void handleGetRequest(String path, PrintWriter out) {
        try {
            if ("/".equals(path) || "/mobile".equals(path)) {
                serveMobileInterface(out);
            } else if ("/api/peers".equals(path)) {
                serveConnectedPeers(out);
            } else if ("/api/messages".equals(path)) {
                serveMessageHistory(out);
            } else if ("/api/status".equals(path)) {
                serveStatus(out);
            } else {
                serve404(out);
            }
        } catch (Exception e) {
            logger.error("Error handling GET request", e);
            serve500(out);
        }
    }
    
    private void handlePostRequest(String path, BufferedReader in, PrintWriter out) {
        try {
            if ("/api/send".equals(path)) {
                handleSendMessage(in, out);
            } else if ("/api/connect".equals(path)) {
                handleConnectPeer(in, out);
            } else {
                serve404(out);
            }
        } catch (Exception e) {
            logger.error("Error handling POST request", e);
            serve500(out);
        }
    }
    
    private void serveMobileInterface(PrintWriter out) {
        String html = generateMobileHTML();
        
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html; charset=utf-8");
        out.println("Content-Length: " + html.getBytes().length);
        out.println("Connection: close");
        out.println();
        out.println(html);
    }
    
    private void serveConnectedPeers(PrintWriter out) {
        try {
            String json = objectMapper.writeValueAsString(networkManager.getConnectedPeers());
            
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + json.getBytes().length);
            out.println("Access-Control-Allow-Origin: *");
            out.println("Connection: close");
            out.println();
            out.println(json);
            
        } catch (Exception e) {
            logger.error("Error serving peers", e);
            serve500(out);
        }
    }
    
    private void serveMessageHistory(PrintWriter out) {
        try {
            String json = objectMapper.writeValueAsString(networkManager.getMessageHistory());
            
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + json.getBytes().length);
            out.println("Access-Control-Allow-Origin: *");
            out.println("Connection: close");
            out.println();
            out.println(json);
            
        } catch (Exception e) {
            logger.error("Error serving messages", e);
            serve500(out);
        }
    }
    
    private void serveStatus(PrintWriter out) {
        try {
            Peer localPeer = networkManager.getLocalPeer();
            Map<String, Object> status = Map.of(
                "localPeer", localPeer,
                "connectedPeers", networkManager.getConnectedPeers().size(),
                "serverPort", WEB_SERVER_PORT,
                "timestamp", LocalDateTime.now()
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
            logger.error("Error serving status", e);
            serve500(out);
        }
    }
    
    private void handleSendMessage(BufferedReader in, PrintWriter out) {
        try {
            // Read POST body (simple implementation)
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                body.append(line);
            }
            
            // Parse message from JSON
            @SuppressWarnings("unchecked")
            Map<String, String> messageData = objectMapper.readValue(body.toString(), Map.class);
            
            String recipientId = messageData.get("recipientId");
            String content = messageData.get("content");
            
            if (recipientId != null && content != null) {
                Message message = new Message(
                    "mobile_web_user", 
                    recipientId, 
                    content
                );
                
                boolean sent = networkManager.sendMessage(message);
                
                String response = objectMapper.writeValueAsString(Map.of("success", sent));
                
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println("Content-Length: " + response.getBytes().length);
                out.println("Access-Control-Allow-Origin: *");
                out.println("Connection: close");
                out.println();
                out.println(response);
            } else {
                serve400(out, "Missing recipientId or content");
            }
            
        } catch (Exception e) {
            logger.error("Error handling send message", e);
            serve500(out);
        }
    }
    
    private void handleConnectPeer(BufferedReader in, PrintWriter out) {
        try {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                body.append(line);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> connectData = objectMapper.readValue(body.toString(), Map.class);
            
            String ip = (String) connectData.get("ip");
            Integer port = (Integer) connectData.get("port");
            
            if (ip != null && port != null) {
                networkManager.connectToPeer(ip, port);
                
                String response = objectMapper.writeValueAsString(Map.of("success", true));
                
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println("Content-Length: " + response.getBytes().length);
                out.println("Access-Control-Allow-Origin: *");
                out.println("Connection: close");
                out.println();
                out.println(response);
            } else {
                serve400(out, "Missing ip or port");
            }
            
        } catch (Exception e) {
            logger.error("Error handling connect peer", e);
            serve500(out);
        }
    }
    
    private String generateMobileHTML() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>P2P Mobile Messaging</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        margin: 0; 
                        padding: 10px; 
                        background-color: #f5f5f5; 
                    }
                    .container { 
                        max-width: 100%; 
                        background: white; 
                        border-radius: 8px; 
                        padding: 15px; 
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1); 
                    }
                    .header { 
                        text-align: center; 
                        color: #2c3e50; 
                        margin-bottom: 20px; 
                    }
                    .peers-section { 
                        margin-bottom: 20px; 
                    }
                    .peer { 
                        padding: 10px; 
                        margin: 5px 0; 
                        background: #e9ecef; 
                        border-radius: 5px; 
                        cursor: pointer; 
                    }
                    .peer:hover { 
                        background: #dee2e6; 
                    }
                    .messages { 
                        height: 300px; 
                        overflow-y: auto; 
                        border: 1px solid #ddd; 
                        padding: 10px; 
                        margin-bottom: 15px; 
                        background: #fafafa; 
                    }
                    .message { 
                        margin: 5px 0; 
                        padding: 8px; 
                        border-radius: 5px; 
                    }
                    .message.own { 
                        background: #007bff; 
                        color: white; 
                        text-align: right; 
                    }
                    .message.other { 
                        background: #e9ecef; 
                    }
                    .input-section { 
                        display: flex; 
                        gap: 10px; 
                    }
                    .input-section input { 
                        flex: 1; 
                        padding: 10px; 
                        border: 1px solid #ddd; 
                        border-radius: 5px; 
                    }
                    .input-section button { 
                        padding: 10px 20px; 
                        background: #007bff; 
                        color: white; 
                        border: none; 
                        border-radius: 5px; 
                        cursor: pointer; 
                    }
                    .refresh-btn { 
                        width: 100%; 
                        padding: 10px; 
                        background: #28a745; 
                        color: white; 
                        border: none; 
                        border-radius: 5px; 
                        margin-bottom: 15px; 
                        cursor: pointer; 
                    }
                    .connect-section {
                        margin-bottom: 20px;
                        padding: 15px;
                        background: #f8f9fa;
                        border-radius: 5px;
                    }
                    .connect-inputs {
                        display: flex;
                        gap: 10px;
                        margin-bottom: 10px;
                    }
                    .connect-inputs input {
                        flex: 1;
                        padding: 8px;
                        border: 1px solid #ddd;
                        border-radius: 3px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📱 P2P Mobile Messaging</h1>
                        <p>Connect your mobile device to the P2P network</p>
                    </div>
                    
                    <div class="connect-section">
                        <h3>Manual Connection</h3>
                        <div class="connect-inputs">
                            <input type="text" id="connectIp" placeholder="IP Address" value="192.168.1.">
                            <input type="number" id="connectPort" placeholder="Port" value="8080">
                            <button onclick="connectToPeer()">Connect</button>
                        </div>
                    </div>
                    
                    <button class="refresh-btn" onclick="refreshPeers()">🔄 Refresh Peers</button>
                    
                    <div class="peers-section">
                        <h3>Connected Peers</h3>
                        <div id="peers"></div>
                    </div>
                    
                    <div class="messages" id="messages">
                        <div class="message other">Welcome to P2P Mobile Messaging!</div>
                        <div class="message other">Select a peer above to start chatting.</div>
                    </div>
                    
                    <div class="input-section">
                        <input type="text" id="messageInput" placeholder="Type your message..." onkeypress="handleKeyPress(event)">
                        <button onclick="sendMessage()">Send</button>
                    </div>
                </div>
                
                <script>
                    let selectedPeer = null;
                    
                    async function refreshPeers() {
                        try {
                            const response = await fetch('/api/peers');
                            const peers = await response.json();
                            
                            const peersDiv = document.getElementById('peers');
                            peersDiv.innerHTML = '';
                            
                            if (peers.length === 0) {
                                peersDiv.innerHTML = '<div>No peers connected. Use manual connection above.</div>';
                            } else {
                                peers.forEach(peer => {
                                    const peerDiv = document.createElement('div');
                                    peerDiv.className = 'peer';
                                    peerDiv.textContent = `${peer.displayName} (${peer.ipAddress}:${peer.port})`;
                                    peerDiv.onclick = () => selectPeer(peer);
                                    peersDiv.appendChild(peerDiv);
                                });
                            }
                        } catch (error) {
                            console.error('Error refreshing peers:', error);
                        }
                    }
                    
                    function selectPeer(peer) {
                        selectedPeer = peer;
                        document.querySelectorAll('.peer').forEach(p => p.style.backgroundColor = '#e9ecef');
                        event.target.style.backgroundColor = '#007bff';
                        event.target.style.color = 'white';
                        
                        const messagesDiv = document.getElementById('messages');
                        messagesDiv.innerHTML = `<div class="message other">Chatting with ${peer.displayName}</div>`;
                        
                        loadMessages();
                    }
                    
                    async function loadMessages() {
                        try {
                            const response = await fetch('/api/messages');
                            const messages = await response.json();
                            
                            const messagesDiv = document.getElementById('messages');
                            
                            messages.forEach(message => {
                                if (selectedPeer && (message.senderId === selectedPeer.id || message.recipientId === selectedPeer.id)) {
                                    const messageDiv = document.createElement('div');
                                    messageDiv.className = `message ${message.senderId === 'mobile_web_user' ? 'own' : 'other'}`;
                                    messageDiv.textContent = message.content;
                                    messagesDiv.appendChild(messageDiv);
                                }
                            });
                            
                            messagesDiv.scrollTop = messagesDiv.scrollHeight;
                        } catch (error) {
                            console.error('Error loading messages:', error);
                        }
                    }
                    
                    async function sendMessage() {
                        if (!selectedPeer) {
                            alert('Please select a peer first');
                            return;
                        }
                        
                        const messageInput = document.getElementById('messageInput');
                        const content = messageInput.value.trim();
                        
                        if (!content) return;
                        
                        try {
                            const response = await fetch('/api/send', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({
                                    recipientId: selectedPeer.id,
                                    content: content
                                })
                            });
                            
                            const result = await response.json();
                            if (result.success) {
                                const messagesDiv = document.getElementById('messages');
                                const messageDiv = document.createElement('div');
                                messageDiv.className = 'message own';
                                messageDiv.textContent = content;
                                messagesDiv.appendChild(messageDiv);
                                messagesDiv.scrollTop = messagesDiv.scrollHeight;
                                
                                messageInput.value = '';
                            } else {
                                alert('Failed to send message');
                            }
                        } catch (error) {
                            console.error('Error sending message:', error);
                            alert('Error sending message');
                        }
                    }
                    
                    async function connectToPeer() {
                        const ip = document.getElementById('connectIp').value.trim();
                        const port = parseInt(document.getElementById('connectPort').value);
                        
                        if (!ip || !port) {
                            alert('Please enter IP address and port');
                            return;
                        }
                        
                        try {
                            const response = await fetch('/api/connect', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ ip: ip, port: port })
                            });
                            
                            const result = await response.json();
                            if (result.success) {
                                alert('Connection initiated to ' + ip + ':' + port);
                                setTimeout(refreshPeers, 2000); // Refresh after 2 seconds
                            } else {
                                alert('Failed to connect');
                            }
                        } catch (error) {
                            console.error('Error connecting:', error);
                            alert('Error connecting to peer');
                        }
                    }
                    
                    function handleKeyPress(event) {
                        if (event.key === 'Enter') {
                            sendMessage();
                        }
                    }
                    
                    // Auto-refresh peers every 10 seconds
                    setInterval(refreshPeers, 10000);
                    
                    // Load initial data
                    refreshPeers();
                </script>
            </body>
            </html>
            """;
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
    
    private void serve400(PrintWriter out, String message) {
        out.println("HTTP/1.1 400 Bad Request");
        out.println("Content-Type: text/plain");
        out.println("Content-Length: " + message.getBytes().length);
        out.println("Connection: close");
        out.println();
        out.println(message);
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
    
    public String getMobileURL() {
        try {
            Peer localPeer = networkManager.getLocalPeer();
            return "http://" + localPeer.getIpAddress() + ":" + WEB_SERVER_PORT + "/mobile";
        } catch (Exception e) {
            return "http://localhost:" + WEB_SERVER_PORT + "/mobile";
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error stopping mobile web server", e);
        }
        executorService.shutdown();
    }
    
    private static class MobileClient {
        private final String id;
        private final Socket socket;
        
        public MobileClient(String id, Socket socket) {
            this.id = id;
            this.socket = socket;
        }
        
        public String getId() { return id; }
        public Socket getSocket() { return socket; }
    }
}