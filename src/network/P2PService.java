package network;

import model.PeerInfo;
import service.MessageService;
import service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * High-level P2P service class that provides easy-to-use APIs for P2P communication.
 * Combines connection management, peer discovery, and message handling.
 */
public class P2PService {
    
    private static P2PService instance;
    
    private final P2PConnectionManager connectionManager;
    private final P2PPeerDiscovery peerDiscovery;
    private final MessageService messageService;
    private volatile boolean initialized;
    
    /**
     * Private constructor for singleton pattern.
     */
    private P2PService() {
        this.connectionManager = P2PConnectionManager.getInstance();
        this.peerDiscovery = new P2PPeerDiscovery(connectionManager.getConfig());
        this.messageService = new MessageService();
        this.initialized = false;
    }
    
    /**
     * Get singleton instance of P2PService.
     * @return P2PService instance
     */
    public static synchronized P2PService getInstance() {
        if (instance == null) {
            instance = new P2PService();
        }
        return instance;
    }
    
    /**
     * Initialize P2P service for current user.
     * Starts the P2P server and registers peer information.
     * @throws IOException if initialization fails
     */
    public void initialize() throws IOException {
        if (initialized) {
            return;
        }
        
        Long userId = UserService.getCurrentUser().getId();
        String username = UserService.getCurrentUser().getUsername();
        
        // Initialize connection manager
        connectionManager.initialize(userId, username);
        
        // Register peer information
        peerDiscovery.registerPeer(userId, username);
        
        initialized = true;
        System.out.println("[P2P Service] Initialized for user: " + username);
    }
    
    /**
     * Connect to a peer by user ID.
     * Looks up peer information and establishes connection.
     * @param peerId Target peer's user ID
     * @return CompletableFuture that completes with chat session
     */
    public CompletableFuture<P2PChatSession> connectToPeer(Long peerId) {
        // Check if already connected
        if (connectionManager.isConnectedTo(peerId)) {
            return CompletableFuture.completedFuture(connectionManager.getSession(peerId));
        }
        
        // Look up peer information
        Optional<PeerInfo> peerInfoOpt = peerDiscovery.findPeer(peerId);
        
        if (peerInfoOpt.isEmpty() || !peerInfoOpt.get().isOnline()) {
            CompletableFuture<P2PChatSession> future = new CompletableFuture<>();
            future.completeExceptionally(new IOException("Peer not online: " + peerId));
            return future;
        }
        
        PeerInfo peerInfo = peerInfoOpt.get();
        return connectionManager.connectToPeer(
            peerInfo.getIpAddress(), 
            peerInfo.getPort(), 
            peerId
        );
    }
    
    /**
     * Send a P2P message to a peer.
     * If not connected, will attempt to connect first.
     * Also persists message to database.
     * @param peerId Target peer's user ID
     * @param content Message content
     * @return CompletableFuture that completes when message is sent
     */
    public CompletableFuture<Boolean> sendMessage(Long peerId, String content) {
        // Persist message to database
        Long currentUserId = UserService.getCurrentUser().getId();
        messageService.sendMessage(currentUserId, peerId, content);
        
        // Send via P2P
        if (connectionManager.isConnectedTo(peerId)) {
            boolean sent = connectionManager.sendMessage(peerId, content);
            return CompletableFuture.completedFuture(sent);
        }
        
        // Try to connect and send
        return connectToPeer(peerId)
            .thenApply(session -> session.sendMessage(content))
            .exceptionally(ex -> {
                System.err.println("[P2P Service] Failed to send message: " + ex.getMessage());
                return false;
            });
    }
    
    /**
     * Send typing indicator to a peer.
     * @param peerId Target peer's user ID
     * @param isTyping Whether user is typing
     */
    public void sendTypingIndicator(Long peerId, boolean isTyping) {
        connectionManager.sendTypingIndicator(peerId, isTyping);
    }
    
    /**
     * Disconnect from a peer.
     * @param peerId Peer's user ID to disconnect from
     */
    public void disconnectPeer(Long peerId) {
        connectionManager.disconnectPeer(peerId);
    }
    
    /**
     * Check if connected to a peer.
     * @param peerId Peer's user ID
     * @return true if connected
     */
    public boolean isConnectedTo(Long peerId) {
        return connectionManager.isConnectedTo(peerId);
    }
    
    /**
     * Get list of online peers (excluding current user).
     * @return List of online peer information
     */
    public List<PeerInfo> getOnlinePeers() {
        Long currentUserId = UserService.getCurrentUser().getId();
        return peerDiscovery.getOnlinePeersExcept(currentUserId);
    }
    
    /**
     * Add a message listener.
     * @param listener The listener to add
     */
    public void addMessageListener(P2PMessageListener listener) {
        connectionManager.addMessageListener(listener);
    }
    
    /**
     * Remove a message listener.
     * @param listener The listener to remove
     */
    public void removeMessageListener(P2PMessageListener listener) {
        connectionManager.removeMessageListener(listener);
    }
    
    /**
     * Get the P2P connection manager.
     * @return Connection manager instance
     */
    public P2PConnectionManager getConnectionManager() {
        return connectionManager;
    }
    
    /**
     * Get the peer discovery service.
     * @return Peer discovery instance
     */
    public P2PPeerDiscovery getPeerDiscovery() {
        return peerDiscovery;
    }
    
    /**
     * Get local P2P address info.
     * @return Address string in format "ip:port"
     */
    public String getLocalAddressInfo() {
        return connectionManager.getLocalAddress() + ":" + connectionManager.getServerPort();
    }
    
    /**
     * Check if service is initialized.
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Shutdown P2P service.
     * Disconnects all peers and stops server.
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }
        
        // Unregister peer
        Long userId = UserService.getCurrentUser().getId();
        peerDiscovery.unregisterPeer(userId);
        
        // Shutdown connection manager
        connectionManager.shutdown();
        
        initialized = false;
        System.out.println("[P2P Service] Shutdown complete");
    }
}

