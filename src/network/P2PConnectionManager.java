package network;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Manages all P2P connections for the current user.
 * Coordinates between P2P server, client, broadcast discovery, and chat sessions.
 * Uses peerName as session key since userId might be same across different databases.
 */
public class P2PConnectionManager {
    
    private static P2PConnectionManager instance;
    
    private Long currentUserId;
    private String currentUsername;
    private P2PServer server;
    private P2PClient client;
    private P2PBroadcastDiscovery broadcastDiscovery;
    
    // Use peerName as key since userId might be duplicate across different DBs
    private final Map<String, P2PChatSession> activeSessionsByName;
    private final Map<Long, P2PChatSession> activeSessionsById;
    private final List<P2PMessageListener> listeners;
    private final List<PeerDiscoveryListener> discoveryListeners;
    private final P2PConfig config;
    
    // Track pending connections to avoid duplicates
    private final Set<String> pendingConnections;
    
    private volatile boolean initialized;
    
    /**
     * Interface for peer discovery events.
     */
    public interface PeerDiscoveryListener {
        void onPeerDiscovered(Long peerId, String peerName, String ipAddress, int port);
        void onPeerLost(Long peerId);
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private P2PConnectionManager() {
        this.activeSessionsByName = new ConcurrentHashMap<>();
        this.activeSessionsById = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.discoveryListeners = new CopyOnWriteArrayList<>();
        this.pendingConnections = ConcurrentHashMap.newKeySet();
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
        
        // Start broadcast discovery for LAN peer discovery
        broadcastDiscovery = new P2PBroadcastDiscovery();
        try {
            broadcastDiscovery.start(userId, username, port, this);
        } catch (IOException e) {
            System.err.println("[P2P Manager] Failed to start broadcast discovery: " + e.getMessage());
            // Continue without broadcast discovery - can still use manual connection
        }
        
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
        String connectionKey = host + ":" + port;
        
        // Check if connection is already pending
        if (pendingConnections.contains(connectionKey)) {
            CompletableFuture<P2PChatSession> future = new CompletableFuture<>();
            future.completeExceptionally(new IOException("Connection already in progress"));
            return future;
        }
        
        // Check if already connected to this host:port
        for (P2PChatSession session : activeSessionsByName.values()) {
            if (session.isActive() && session.getPeerAddress().startsWith(host)) {
                return CompletableFuture.completedFuture(session);
            }
        }
        
        pendingConnections.add(connectionKey);
        
        return client.connectAsync(host, port, peerId)
            .whenComplete((session, ex) -> {
                pendingConnections.remove(connectionKey);
            });
    }
    
    /**
     * Send a message to a peer by ID.
     * @param peerId Target peer's ID
     * @param content Message content
     * @return true if message was sent
     */
    public boolean sendMessage(Long peerId, String content) {
        P2PChatSession session = activeSessionsById.get(peerId);
        if (session != null && session.isActive()) {
            return session.sendMessage(content);
        }
        return false;
    }
    
    /**
     * Send a message to a peer by name.
     * @param peerName Target peer's name
     * @param content Message content
     * @return true if message was sent
     */
    public boolean sendMessageByName(String peerName, String content) {
        P2PChatSession session = activeSessionsByName.get(peerName);
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
        P2PChatSession session = activeSessionsById.get(peerId);
        if (session != null && session.isActive()) {
            session.sendTypingIndicator(isTyping);
        }
    }
    
    /**
     * Disconnect from a peer.
     * @param peerId Peer's ID to disconnect from
     */
    public void disconnectPeer(Long peerId) {
        P2PChatSession session = activeSessionsById.get(peerId);
        if (session != null) {
            session.close();
        }
    }
    
    /**
     * Check if already connected to a peer from specific address.
     * @param host Peer's host address
     * @return true if already connected
     */
    public boolean isConnectedToHost(String host) {
        for (P2PChatSession session : activeSessionsByName.values()) {
            if (session.isActive() && session.getPeerAddress().startsWith(host)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Register a chat session.
     * @param peerId Peer's user ID
     * @param peerName Peer's username
     * @param session The chat session
     * @return true if registered, false if duplicate connection
     */
    public boolean registerSession(Long peerId, String peerName, P2PChatSession session) {
        // Check for existing connection by name
        P2PChatSession existingByName = activeSessionsByName.get(peerName);
        if (existingByName != null && existingByName.isActive() && existingByName != session) {
            // Already have an active connection to this peer
            System.out.println("[P2P Manager] Already connected to " + peerName + ", rejecting duplicate");
            return false;
        }
        
        // Register by name (primary key)
        activeSessionsByName.put(peerName, session);
        
        // Also register by ID for compatibility
        activeSessionsById.put(peerId, session);
        
        System.out.println("[P2P Manager] Registered session with: " + peerName + " (ID: " + peerId + ")");
        return true;
    }
    
    /**
     * Register a chat session (legacy method for compatibility).
     * @param peerId Peer's user ID
     * @param session The chat session
     */
    public void registerSession(Long peerId, P2PChatSession session) {
        registerSession(peerId, session.getPeerName(), session);
    }
    
    /**
     * Unregister a chat session.
     * @param peerId Peer's user ID
     */
    public void unregisterSession(Long peerId) {
        P2PChatSession session = activeSessionsById.remove(peerId);
        if (session != null) {
            activeSessionsByName.remove(session.getPeerName());
            System.out.println("[P2P Manager] Unregistered session: " + session.getPeerName());
        }
    }
    
    /**
     * Unregister a chat session by name.
     * @param peerName Peer's username
     */
    public void unregisterSessionByName(String peerName) {
        P2PChatSession session = activeSessionsByName.remove(peerName);
        if (session != null) {
            activeSessionsById.remove(session.getPeerId());
        }
    }
    
    /**
     * Check if connected to a peer by ID.
     * @param peerId Peer's user ID
     * @return true if connected
     */
    public boolean isConnectedTo(Long peerId) {
        P2PChatSession session = activeSessionsById.get(peerId);
        return session != null && session.isActive();
    }
    
    /**
     * Check if connected to a peer by name.
     * @param peerName Peer's username
     * @return true if connected
     */
    public boolean isConnectedToByName(String peerName) {
        P2PChatSession session = activeSessionsByName.get(peerName);
        return session != null && session.isActive();
    }
    
    /**
     * Get active session with a peer by ID.
     * @param peerId Peer's user ID
     * @return Chat session or null if not connected
     */
    public P2PChatSession getSession(Long peerId) {
        return activeSessionsById.get(peerId);
    }
    
    /**
     * Get active session with a peer by name.
     * @param peerName Peer's username
     * @return Chat session or null if not connected
     */
    public P2PChatSession getSessionByName(String peerName) {
        return activeSessionsByName.get(peerName);
    }
    
    /**
     * Get all active peer IDs.
     * @return Set of connected peer IDs
     */
    public Set<Long> getConnectedPeerIds() {
        return new HashSet<>(activeSessionsById.keySet());
    }
    
    /**
     * Get all active peer names.
     * @return Set of connected peer names
     */
    public Set<String> getConnectedPeerNames() {
        return new HashSet<>(activeSessionsByName.keySet());
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
     * Add a peer discovery listener.
     * @param listener The listener to add
     */
    public void addDiscoveryListener(PeerDiscoveryListener listener) {
        discoveryListeners.add(listener);
    }
    
    /**
     * Remove a peer discovery listener.
     * @param listener The listener to remove
     */
    public void removeDiscoveryListener(PeerDiscoveryListener listener) {
        discoveryListeners.remove(listener);
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
     * Notify listeners of a newly discovered peer via broadcast.
     * @param peerId Discovered peer's ID
     * @param peerName Discovered peer's name
     * @param ipAddress Peer's IP address
     * @param port Peer's P2P server port
     */
    public void notifyPeerDiscovered(Long peerId, String peerName, String ipAddress, int port) {
        for (PeerDiscoveryListener listener : discoveryListeners) {
            try {
                listener.onPeerDiscovered(peerId, peerName, ipAddress, port);
            } catch (Exception e) {
                System.err.println("[P2P Manager] Discovery listener error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get the broadcast discovery service.
     * @return Broadcast discovery instance or null if not available
     */
    public P2PBroadcastDiscovery getBroadcastDiscovery() {
        return broadcastDiscovery;
    }
    
    /**
     * Force announce presence on the network.
     */
    public void forceAnnounce() {
        if (broadcastDiscovery != null && broadcastDiscovery.isRunning()) {
            broadcastDiscovery.forceAnnounce();
        }
    }
    
    /**
     * Shutdown the P2P connection manager.
     */
    public void shutdown() {
        // Close all sessions
        for (P2PChatSession session : activeSessionsByName.values()) {
            session.close();
        }
        activeSessionsByName.clear();
        activeSessionsById.clear();
        pendingConnections.clear();
        
        // Stop broadcast discovery
        if (broadcastDiscovery != null) {
            broadcastDiscovery.stop();
            broadcastDiscovery = null;
        }
        
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
