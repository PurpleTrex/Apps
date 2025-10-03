package com.p2p.messaging.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.p2p.messaging.model.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Service for discovering other P2P peers on the local network
 * Uses UDP broadcast to announce presence and discover other peers
 */
public class PeerDiscoveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(PeerDiscoveryService.class);
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    private static final long ANNOUNCEMENT_INTERVAL = 30000; // 30 seconds
    
    private final int port;
    private final Peer localPeer;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean running;
    private final ExecutorService executorService;
    
    private DatagramSocket socket;
    private Consumer<Peer> peerDiscoveredCallback;
    
    public PeerDiscoveryService(int port, Peer localPeer) {
        this.port = port;
        this.localPeer = localPeer;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.running = new AtomicBoolean(false);
        this.executorService = Executors.newFixedThreadPool(2);
    }
    
    public void setPeerDiscoveredCallback(Consumer<Peer> callback) {
        this.peerDiscoveredCallback = callback;
    }
    
    public void start() {
        if (running.compareAndSet(false, true)) {
            try {
                socket = new DatagramSocket(port);
                socket.setBroadcast(true);
                
                // Start listener thread
                executorService.submit(this::listenForPeers);
                
                // Start announcement thread
                executorService.submit(this::announcePeer);
                
                logger.info("Peer discovery service started on port {}", port);
                
            } catch (Exception e) {
                logger.error("Failed to start peer discovery service", e);
                running.set(false);
            }
        }
    }
    
    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
            executorService.shutdown();
            logger.info("Peer discovery service stopped");
        }
    }
    
    private void listenForPeers() {
        byte[] buffer = new byte[1024];
        
        while (running.get() && socket != null && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                processPeerAnnouncement(message, packet.getAddress());
                
            } catch (SocketException e) {
                if (running.get()) {
                    logger.error("Socket error in peer discovery", e);
                }
                break;
            } catch (Exception e) {
                logger.error("Error receiving peer announcement", e);
            }
        }
    }
    
    private void announcePeer() {
        while (running.get()) {
            try {
                broadcastPeerAnnouncement();
                Thread.sleep(ANNOUNCEMENT_INTERVAL);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error broadcasting peer announcement", e);
            }
        }
    }
    
    private void broadcastPeerAnnouncement() {
        try {
            String announcement = createPeerAnnouncement();
            byte[] data = announcement.getBytes();
            
            InetAddress broadcastAddr = InetAddress.getByName(BROADCAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddr, port);
            
            socket.send(packet);
            logger.debug("Broadcast peer announcement sent");
            
        } catch (Exception e) {
            logger.error("Failed to broadcast peer announcement", e);
        }
    }
    
    private String createPeerAnnouncement() throws Exception {
        return objectMapper.writeValueAsString(localPeer);
    }
    
    private void processPeerAnnouncement(String message, InetAddress senderAddress) {
        try {
            Peer discoveredPeer = objectMapper.readValue(message, Peer.class);
            
            // Ignore announcements from ourselves
            if (discoveredPeer.getId().equals(localPeer.getId())) {
                return;
            }
            
            // Update peer's IP address from the packet sender
            discoveredPeer.setIpAddress(senderAddress.getHostAddress());
            discoveredPeer.setStatus(Peer.Status.ONLINE);
            
            logger.debug("Discovered peer: {} at {}", discoveredPeer.getUsername(), discoveredPeer.getConnectionAddress());
            
            if (peerDiscoveredCallback != null) {
                peerDiscoveredCallback.accept(discoveredPeer);
            }
            
        } catch (Exception e) {
            logger.debug("Invalid peer announcement received: {}", message);
        }
    }
}