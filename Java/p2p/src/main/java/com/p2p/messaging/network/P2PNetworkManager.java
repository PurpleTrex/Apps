package com.p2p.messaging.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.p2p.messaging.model.Message;
import com.p2p.messaging.model.Peer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Manages P2P network connections and message routing
 * Supports multiple protocols including RCS and iOS integration
 */
public class P2PNetworkManager {
    
    private static final Logger logger = LoggerFactory.getLogger(P2PNetworkManager.class);
    private static final int DEFAULT_PORT = 8080;
    private static final int DISCOVERY_PORT = 8081;
    
    private final ObjectMapper objectMapper;
    private final Map<String, Peer> connectedPeers;
    private final Map<String, Channel> peerChannels;
    private final List<Message> messageHistory;
    private final List<Consumer<Message>> messageListeners;
    private final List<Consumer<Peer>> peerListeners;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private PeerDiscoveryService discoveryService;
    private MobileWebServer mobileWebServer;
    private IPChannelService ipChannelService;
    private Peer localPeer;
    private int serverPort;
    
    public P2PNetworkManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        this.connectedPeers = new ConcurrentHashMap<>();
        this.peerChannels = new ConcurrentHashMap<>();
        this.messageHistory = new CopyOnWriteArrayList<>();
        this.messageListeners = new CopyOnWriteArrayList<>();
        this.peerListeners = new CopyOnWriteArrayList<>();
        
        this.serverPort = DEFAULT_PORT;
        
        initializeLocalPeer();
        startServer();
        startDiscoveryService();
        startMobileWebServer();
        startIPChannelService();
    }
    
    private void initializeLocalPeer() {
        try {
            String localIp = getLocalIPAddress();
            String username = System.getProperty("user.name", "User");
            String peerId = UUID.randomUUID().toString();
            
            localPeer = new Peer(peerId, username, username, localIp, serverPort);
            localPeer.setStatus(Peer.Status.ONLINE);
            
            logger.info("Local peer initialized: {} at {}:{}", username, localIp, serverPort);
            
        } catch (Exception e) {
            logger.error("Failed to initialize local peer", e);
            throw new RuntimeException("Network initialization failed", e);
        }
    }
    
    private void startServer() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new P2PMessageHandler()
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            ChannelFuture future = bootstrap.bind(serverPort).sync();
            serverChannel = future.channel();
            
            logger.info("P2P server started on port {}", serverPort);
            
        } catch (Exception e) {
            logger.error("Failed to start P2P server", e);
            throw new RuntimeException("Server startup failed", e);
        }
    }
    
    private void startDiscoveryService() {
        discoveryService = new PeerDiscoveryService(DISCOVERY_PORT, localPeer);
        discoveryService.setPeerDiscoveredCallback(this::onPeerDiscovered);
        discoveryService.start();
    }
    
    private void startMobileWebServer() {
        mobileWebServer = new MobileWebServer(this);
        mobileWebServer.start();
        logger.info("Mobile web interface available at: {}", mobileWebServer.getMobileURL());
    }
    
    private void startIPChannelService() {
        ipChannelService = new IPChannelService(this);
        ipChannelService.start();
        logger.info("IP Channel service started - create channels for public messaging");
    }
    
    public void connectToPeer(String ipAddress, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(
                                new StringDecoder(),
                                new StringEncoder(),
                                new P2PMessageHandler()
                        );
                    }
                });
        
        try {
            ChannelFuture future = bootstrap.connect(ipAddress, port);
            future.addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    Channel channel = channelFuture.channel();
                    logger.info("Connected to peer at {}:{}", ipAddress, port);
                    
                    // Send handshake message
                    sendHandshake(channel);
                } else {
                    logger.error("Failed to connect to peer at {}:{}", ipAddress, port, channelFuture.cause());
                }
            });
            
        } catch (Exception e) {
            logger.error("Error connecting to peer at {}:{}", ipAddress, port, e);
        }
    }
    
    public boolean sendMessage(Message message) {
        try {
            String recipientId = message.getRecipientId();
            Channel channel = peerChannels.get(recipientId);
            
            // Always add to message history first, regardless of connection status
            messageHistory.add(message);
            
            if (channel != null && channel.isActive()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                channel.writeAndFlush(jsonMessage);
                message.setDelivered(true);
                logger.debug("Message sent to peer {}: {}", recipientId, message.getDisplayText());
            } else {
                // Peer is offline - store message for later delivery
                message.setDelivered(false);
                logger.info("Peer {} is offline - message stored for later delivery: {}", 
                          recipientId, message.getDisplayText());
                
                // Check if this is a virtual channel peer
                Peer peer = connectedPeers.get(recipientId);
                if (peer != null && "channel".equals(peer.getIpAddress())) {
                    logger.info("Message sent to virtual channel peer: {}", peer.getDisplayName());
                }
            }
            
                            // Check if this message is going to a channel peer and notify IP channel service
            Peer recipient = connectedPeers.get(recipientId);
            if (recipient != null && "channel".equals(recipient.getIpAddress()) && ipChannelService != null) {
                // This is a message to a channel peer - notify the IP channel service
                logger.info("Notifying IP channel service of desktop message to channel peer: {}", recipient.getDisplayName());
                ipChannelService.handleDesktopMessage(message);
            } else if (recipient != null) {
                logger.debug("Message sent to regular peer (IP: {}): {}", recipient.getIpAddress(), recipient.getDisplayName());
            } else {
                logger.debug("Recipient peer not found for ID: {}", recipientId);
            }            // Always notify listeners so the UI shows the message
            notifyMessageListeners(message);
            
            // Always return true now - message is stored even if not immediately delivered
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send message", e);
            return false;
        }
    }
    
    public void addMessageListener(Consumer<Message> listener) {
        messageListeners.add(listener);
    }
    
    public void removeMessageListener(Consumer<Message> listener) {
        messageListeners.remove(listener);
    }
    
    /**
     * Handle an external message (e.g., from IP channel)
     */
    public void handleExternalMessage(Message message) {
        // Add to message history
        messageHistory.add(message);
        
        // Notify all listeners
        notifyMessageListeners(message);
        
        logger.info("Received external message from {}: {}", message.getSenderId(), message.getContent());
    }
    
    /**
     * Handle a virtual peer from IP channel
     */
    public void handleChannelPeer(Peer virtualPeer) {
        // Add to connected peers if not already present
        if (!connectedPeers.containsKey(virtualPeer.getId())) {
            connectedPeers.put(virtualPeer.getId(), virtualPeer);
            
            // Notify peer listeners
            notifyPeerListeners(virtualPeer);
            
            logger.info("Added virtual channel peer: {} ({})", 
                       virtualPeer.getDisplayName(), virtualPeer.getId());
        }
    }
    
    public void addPeerListener(Consumer<Peer> listener) {
        peerListeners.add(listener);
    }
    
    public void removePeerListener(Consumer<Peer> listener) {
        peerListeners.remove(listener);
    }
    
    private void onPeerDiscovered(Peer peer) {
        if (!peer.equals(localPeer) && !connectedPeers.containsKey(peer.getId())) {
            logger.info("Discovered new peer: {}", peer);
            connectToPeer(peer.getIpAddress(), peer.getPort());
        }
    }
    
    private void sendHandshake(Channel channel) {
        try {
            Map<String, Object> handshake = new HashMap<>();
            handshake.put("type", "HANDSHAKE");
            handshake.put("peer", localPeer);
            
            String jsonHandshake = objectMapper.writeValueAsString(handshake);
            channel.writeAndFlush(jsonHandshake);
            
        } catch (Exception e) {
            logger.error("Failed to send handshake", e);
        }
    }
    
    private void handleHandshake(Channel channel, Map<String, Object> data) {
        try {
            Peer peer = objectMapper.convertValue(data.get("peer"), Peer.class);
            
            connectedPeers.put(peer.getId(), peer);
            peerChannels.put(peer.getId(), channel);
            
            logger.info("Peer connected: {}", peer);
            notifyPeerListeners(peer);
            
        } catch (Exception e) {
            logger.error("Failed to handle handshake", e);
        }
    }
    
    private void handleMessage(String jsonMessage) {
        try {
            if (jsonMessage.contains("\"type\":\"HANDSHAKE\"")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> handshakeData = objectMapper.readValue(jsonMessage, Map.class);
                handleHandshake(null, handshakeData);
                return;
            }
            
            Message message = objectMapper.readValue(jsonMessage, Message.class);
            messageHistory.add(message);
            
            logger.debug("Received message: {}", message.getDisplayText());
            notifyMessageListeners(message);
            
        } catch (Exception e) {
            logger.error("Failed to handle incoming message: {}", jsonMessage, e);
        }
    }
    
    private void notifyMessageListeners(Message message) {
        for (Consumer<Message> listener : messageListeners) {
            try {
                listener.accept(message);
            } catch (Exception e) {
                logger.error("Error in message listener", e);
            }
        }
    }
    
    private void notifyPeerListeners(Peer peer) {
        for (Consumer<Peer> listener : peerListeners) {
            try {
                listener.accept(peer);
            } catch (Exception e) {
                logger.error("Error in peer listener", e);
            }
        }
    }
    
    private String getLocalIPAddress() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }
            
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                
                if (!address.isLoopbackAddress() && address.isSiteLocalAddress()) {
                    return address.getHostAddress();
                }
            }
        }
        
        return InetAddress.getLocalHost().getHostAddress();
    }
    
    public Peer getLocalPeer() {
        return localPeer;
    }
    
    public Collection<Peer> getConnectedPeers() {
        return new ArrayList<>(connectedPeers.values());
    }
    
    public List<Message> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }
    
    public String getMobileWebURL() {
        return mobileWebServer != null ? mobileWebServer.getMobileURL() : null;
    }
    
    public IPChannelService getIPChannelService() {
        return ipChannelService;
    }
    
    public void shutdown() {
        logger.info("Shutting down P2P network manager...");
        
        if (discoveryService != null) {
            discoveryService.stop();
        }
        
        if (mobileWebServer != null) {
            mobileWebServer.stop();
        }
        
        if (ipChannelService != null) {
            ipChannelService.stop();
        }
        
        if (serverChannel != null) {
            serverChannel.close();
        }
        
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        
        logger.info("P2P network manager shutdown complete");
    }
    
    private class P2PMessageHandler extends SimpleChannelInboundHandler<String> {
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            handleMessage(msg);
        }
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.debug("Channel active: {}", ctx.channel().remoteAddress());
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            logger.debug("Channel inactive: {}", ctx.channel().remoteAddress());
            
            // Remove peer from connected list
            String peerId = findPeerIdByChannel(ctx.channel());
            if (peerId != null) {
                connectedPeers.remove(peerId);
                peerChannels.remove(peerId);
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.error("Channel exception", cause);
            ctx.close();
        }
    }
    
    private String findPeerIdByChannel(Channel channel) {
        for (Map.Entry<String, Channel> entry : peerChannels.entrySet()) {
            if (entry.getValue().equals(channel)) {
                return entry.getKey();
            }
        }
        return null;
    }
}