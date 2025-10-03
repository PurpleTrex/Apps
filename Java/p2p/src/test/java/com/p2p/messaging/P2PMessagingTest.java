package com.p2p.messaging;

import com.p2p.messaging.model.Message;
import com.p2p.messaging.model.Peer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic unit tests for P2P Messaging components
 */
public class P2PMessagingTest {
    
    @Test
    public void testMessageCreation() {
        String senderId = "sender123";
        String recipientId = "recipient456";
        String content = "Hello, World!";
        
        Message message = new Message(senderId, recipientId, content);
        
        assertNotNull(message.getId());
        assertEquals(senderId, message.getSenderId());
        assertEquals(recipientId, message.getRecipientId());
        assertEquals(content, message.getContent());
        assertEquals(Message.MessageType.TEXT, message.getMessageType());
        assertEquals(Message.Protocol.P2P, message.getProtocol());
        assertNotNull(message.getTimestamp());
    }
    
    @Test
    public void testPeerCreation() {
        String id = "peer123";
        String username = "testuser";
        String displayName = "Test User";
        
        Peer peer = new Peer(id, username, displayName);
        
        assertEquals(id, peer.getId());
        assertEquals(username, peer.getUsername());
        assertEquals(displayName, peer.getDisplayName());
        assertEquals(Peer.Status.OFFLINE, peer.getStatus());
        assertNotNull(peer.getLastSeen());
        assertFalse(peer.isBlocked());
    }
    
    @Test
    public void testFileMessage() {
        String senderId = "sender123";
        String recipientId = "recipient456";
        String fileName = "test.jpg";
        byte[] fileData = "fake image data".getBytes();
        
        Message fileMessage = new Message(senderId, recipientId, fileName, fileData, Message.MessageType.IMAGE);
        
        assertEquals(Message.MessageType.IMAGE, fileMessage.getMessageType());
        assertEquals(fileName, fileMessage.getFileName());
        assertEquals(fileData.length, fileMessage.getFileSize());
        assertTrue(fileMessage.isFileMessage());
        assertEquals("📷 Image: " + fileName, fileMessage.getDisplayText());
    }
    
    @Test
    public void testPeerStatus() {
        Peer peer = new Peer("id", "user", "User");
        
        peer.setStatus(Peer.Status.ONLINE);
        assertTrue(peer.isOnline());
        assertEquals("🟢", peer.getStatusIcon());
        
        peer.setStatus(Peer.Status.AWAY);
        assertFalse(peer.isOnline());
        assertEquals("🟡", peer.getStatusIcon());
        
        peer.setStatus(Peer.Status.BUSY);
        assertEquals("🔴", peer.getStatusIcon());
        
        peer.setStatus(Peer.Status.OFFLINE);
        assertEquals("⚫", peer.getStatusIcon());
    }
}