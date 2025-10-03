package com.p2p.messaging.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a peer in the P2P messaging network
 */
public class Peer {
    
    public enum Status {
        ONLINE, OFFLINE, AWAY, BUSY
    }
    
    private final String id;
    private final String username;
    private final String displayName;
    private String ipAddress;
    private int port;
    private Status status;
    private LocalDateTime lastSeen;
    private String avatarPath;
    private boolean isBlocked;
    
    public Peer(String id, String username, String displayName) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.status = Status.OFFLINE;
        this.lastSeen = LocalDateTime.now();
        this.isBlocked = false;
    }
    
    public Peer(String id, String username, String displayName, String ipAddress, int port) {
        this(id, username, displayName);
        this.ipAddress = ipAddress;
        this.port = port;
    }
    
    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getIpAddress() { return ipAddress; }
    public int getPort() { return port; }
    public Status getStatus() { return status; }
    public LocalDateTime getLastSeen() { return lastSeen; }
    public String getAvatarPath() { return avatarPath; }
    public boolean isBlocked() { return isBlocked; }
    
    // Setters
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setPort(int port) { this.port = port; }
    public void setStatus(Status status) { 
        this.status = status; 
        if (status == Status.ONLINE) {
            this.lastSeen = LocalDateTime.now();
        }
    }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }
    
    public boolean isOnline() {
        return status == Status.ONLINE;
    }
    
    public String getStatusIcon() {
        switch (status) {
            case ONLINE: return "🟢";
            case AWAY: return "🟡";
            case BUSY: return "🔴";
            case OFFLINE: 
            default: return "⚫";
        }
    }
    
    public String getConnectionAddress() {
        return ipAddress != null ? ipAddress + ":" + port : "N/A";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Peer peer = (Peer) obj;
        return Objects.equals(id, peer.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Peer{id='%s', username='%s', displayName='%s', status=%s}", 
                           id, username, displayName, status);
    }
}