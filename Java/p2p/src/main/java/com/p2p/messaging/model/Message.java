package com.p2p.messaging.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a message in the P2P messaging system
 * Supports various message types including text, images, videos, and documents
 */
public class Message {
    
    public enum MessageType {
        TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT, FILE
    }
    
    public enum Protocol {
        P2P, RCS, IOS, SMS
    }
    
    private final String id;
    private final String senderId;
    private final String recipientId;
    private final String content;
    private final MessageType messageType;
    private final Protocol protocol;
    private final LocalDateTime timestamp;
    private final String fileName;
    private final long fileSize;
    private final byte[] fileData;
    private boolean delivered;
    private boolean read;
    
    @JsonCreator
    public Message(
            @JsonProperty("id") String id,
            @JsonProperty("senderId") String senderId,
            @JsonProperty("recipientId") String recipientId,
            @JsonProperty("content") String content,
            @JsonProperty("messageType") MessageType messageType,
            @JsonProperty("protocol") Protocol protocol,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("fileName") String fileName,
            @JsonProperty("fileSize") long fileSize,
            @JsonProperty("fileData") byte[] fileData) {
        
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.messageType = messageType;
        this.protocol = protocol != null ? protocol : Protocol.P2P;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileData = fileData;
        this.delivered = false;
        this.read = false;
    }
    
    // Constructor for text messages
    public Message(String senderId, String recipientId, String content) {
        this(null, senderId, recipientId, content, MessageType.TEXT, Protocol.P2P, null, null, 0, null);
    }
    
    // Constructor for file messages
    public Message(String senderId, String recipientId, String fileName, byte[] fileData, MessageType messageType) {
        this(null, senderId, recipientId, null, messageType, Protocol.P2P, null, fileName, 
             fileData != null ? fileData.length : 0, fileData);
    }
    
    // Getters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getRecipientId() { return recipientId; }
    public String getContent() { return content; }
    public MessageType getMessageType() { return messageType; }
    public Protocol getProtocol() { return protocol; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public byte[] getFileData() { return fileData; }
    public boolean isDelivered() { return delivered; }
    public boolean isRead() { return read; }
    
    // Setters for status
    public void setDelivered(boolean delivered) { this.delivered = delivered; }
    public void setRead(boolean read) { this.read = read; }
    
    public boolean isFileMessage() {
        return messageType != MessageType.TEXT && fileData != null;
    }
    
    public String getDisplayText() {
        switch (messageType) {
            case TEXT:
                return content;
            case IMAGE:
                return "📷 Image: " + (fileName != null ? fileName : "image");
            case VIDEO:
                return "🎥 Video: " + (fileName != null ? fileName : "video");
            case AUDIO:
                return "🎵 Audio: " + (fileName != null ? fileName : "audio");
            case DOCUMENT:
                return "📄 Document: " + (fileName != null ? fileName : "document");
            case FILE:
                return "📁 File: " + (fileName != null ? fileName : "file");
            default:
                return "Unknown message type";
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Message message = (Message) obj;
        return Objects.equals(id, message.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Message{id='%s', sender='%s', recipient='%s', type=%s, timestamp=%s}", 
                           id, senderId, recipientId, messageType, timestamp);
    }
}