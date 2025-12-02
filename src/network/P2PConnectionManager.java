package network;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Manages all P2P connections for the current user.
 * Coordinates between P2P server, client, and chat sessions.
 */
public class P2PConnectionManager {
    
    private static P2PConnectionManager instance;
    
    private Long currentUserId;
    private String currentUsername;
    private P2PServer server;
    private P2PClient client;
    private final Map<Long, P2PChatSession> activeSessions;
    private final List<P2PMessageListener> listeners;
    private final P2PConfig config;
    
    private volatile boolean initialized;
    
    /**
     * Private constructor for singleton pattern.
     */
    private P2PConnectionManager() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.config = new P2PConfig();
        this.initialized = false;
    }
    
    /**
     * Get the singleton instance of P2PConnectionManager.
     * @return P2PConnectionManager instance
     */
    public static synchronized P2PConnectionManager getInstance() {
        if (instance == null) {
            instance = new P2PConnectionManager();
        }
        return instance;
    }
    
    /**
     * Initialize the P2P connection manager for a user.
     * @param userId Current user's ID
     * @param username Current user's username
     * @throws IOException if server cannot be started
     */
    public void initialize(Long userId, String username) throws IOException {
        if (initialized) {
            shutdown();
        }
        
        this.currentUserId = userId;
        this.currentUsername = username;
        
        // Start P2P server
        int port = config.getBasePort() + userId.intValue() % 1000;
        server = new P2PServer(port, userId, username, this);
        server.start();
        
        // Create P2P client
        client = new P2PClient(userId, username, this);
        
        initialized = true;
        System.out.println("[P2P Manager] Initialized for user: " + username + 
                          " on port: " + port);
    }
    
    /**
     * Connect to a peer.
     * @param host Peer's host address
     * @param port Peer's port number
     * @param peerId Peer's user ID
     * @return CompletableFuture that completes with the chat session
     */
    public CompletableFuture<P2PChatSession> connectToPeer(String host, int port, Long peerId) {
        // Check if already connected
        if (activeSessions.containsKey(peerId)) {
            return CompletableFuture.completedFuture(activeSessions.get(peerId));
        }
        
        return client.connectAsync(host, port, peerId);
    }
    
    /**
     * Send a message to a peer.
     * @param peerId Target peer's ID
     * @param content Message content
     * @return true if message was sent
     */
    public boolean sendMessage(Long peerId, String content) {
        P2PChatSession session = activeSessions.get(peerId);
        if (session != null && session.isActive()) {
            return session.sendMessage(content);
        }
        return false;
    }
    
    /**
     * Send typing indicator to a peer.
     * @param peerId Target peer's ID
     * @param isTyping Whether user is typing
     */
    public void sendTypingIndicator(Long peerId, boolean isTyping) {
        P2PChatSession session = activeSessions.get(peerId);
        if (session != null && session.isActive()) {
            session.sendTypingIndicator(isTyping);
        }
    }
    
    /**
     * Disconnect from a peer.
     * @param peerId Peer's ID to disconnect from
     */
    public void disconnectPeer(Long peerId) {
        P2PChatSession session = activeSessions.get(peerId);
        if (session != null) {
            session.close();
        }
    }
    
    /**
     * Register a chat session.
     * @param peerId Peer's user ID
     * @param session The chat session
     */
    public void registerSession(Long peerId, P2PChatSession session) {
        // Close existing session if any
        P2PChatSession existing = activeSessions.put(peerId, session);
        if (existing != null && existing != session) {
            existing.close();
        }
    }
    
    /**
     * Unregister a chat session.
     * @param peerId Peer's user ID
     */
    public void unregisterSession(Long peerId) {
        activeSessions.remove(peerId);
    }
    
    /**
     * Check if connected to a peer.
     * @param peerId Peer's user ID
     * @return true if connected
     */
    public boolean isConnectedTo(Long peerId) {
        P2PChatSession session = activeSessions.get(peerId);
        return session != null && session.isActive();
    }
    
    /**
     * Get active session with a peer.
     * @param peerId Peer's user ID
     * @return Chat session or null if not connected
     */
    public P2PChatSession getSession(Long peerId) {
        return activeSessions.get(peerId);
    }
    
    /**
     * Get all active peer IDs.
     * @return Set of connected peer IDs
     */
    public Set<Long> getConnectedPeerIds() {
        return new HashSet<>(activeSessions.keySet());
    }
    
    /**
     * Add a message listener.
     * @param listener The listener to add
     */
    public void addMessageListener(P2PMessageListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a message listener.
     * @param listener The listener to remove
     */
    public void removeMessageListener(P2PMessageListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify listeners of a received message.
     * @param message The received message
     */
    public void notifyMessageReceived(P2PMessage message) {
        for (P2PMessageListener listener : listeners) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                System.err.println("[P2P Manager] Listener error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners of peer connection.
     * @param peerId Connected peer's ID
     * @param peerName Connected peer's name
     */
    public void notifyPeerConnected(Long peerId, String peerName) {
        for (P2PMessageListener listener : listeners) {
            try {
                listener.onPeerConnected(peerId, peerName);
            } catch (Exception e) {
                System.err.println("[P2P Manager] Listener error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners of peer disconnection.
     * @param peerId Disconnected peer's ID
     */
    public void notifyPeerDisconnected(Long peerId) {
        for (P2PMessageListener listener : listeners) {
            try {
                listener.onPeerDisconnected(peerId);
            } catch (Exception e) {
                System.err.println("[P2P Manager] Listener error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners of typing status change.
     * @param peerId Peer's ID
     * @param isTyping Whether peer is typing
     */
    public void notifyTypingStatus(Long peerId, boolean isTyping) {
        for (P2PMessageListener listener : listeners) {
            try {
                listener.onTypingStatusChanged(peerId, isTyping);
            } catch (Exception e) {
                System.err.println("[P2P Manager] Listener error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners of an error.
     * @param error Error message
     */
    public void notifyError(String error) {
        for (P2PMessageListener listener : listeners) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                System.err.println("[P2P Manager] Listener error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Shutdown the P2P connection manager.
     */
    public void shutdown() {
        // Close all sessions
        for (P2PChatSession session : activeSessions.values()) {
            session.close();
        }
        activeSessions.clear();
        
        // Stop server
        if (server != null) {
            server.stop();
            server = null;
        }
        
        // Shutdown client
        if (client != null) {
            client.shutdown();
            client = null;
        }
        
        initialized = false;
        System.out.println("[P2P Manager] Shutdown complete");
    }
    
    /**
     * Get current user's ID.
     * @return Current user ID
     */
    public Long getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * Get current user's username.
     * @return Current username
     */
    public String getCurrentUsername() {
        return currentUsername;
    }
    
    /**
     * Get the server's local address.
     * @return Local address string
     */
    public String getLocalAddress() {
        return server != null ? server.getLocalAddress() : "127.0.0.1";
    }
    
    /**
     * Get the server's port.
     * @return Server port number
     */
    public int getServerPort() {
        return server != null ? server.getPort() : 0;
    }
    
    /**
     * Check if manager is initialized.
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get the P2P configuration.
     * @return P2P configuration object
     */
    public P2PConfig getConfig() {
        return config;
    }
}

