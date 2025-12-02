package network;

import model.PeerInfo;
import service.MessageService;
import service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * High-level P2P service class that provides easy-to-use APIs for P2P communication.
 * Combines connection management, peer discovery (both database and broadcast), and message handling.
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
     * Starts the P2P server, broadcast discovery, and registers peer information.
     * @throws IOException if initialization fails
     */
    public void initialize() throws IOException {
        if (initialized) {
            return;
        }
        
        Long userId = UserService.getCurrentUser().getId();
        String username = UserService.getCurrentUser().getUsername();
        
        // Initialize connection manager (this also starts broadcast discovery)
        connectionManager.initialize(userId, username);
        
        // Register peer information in local database (for reference)
        peerDiscovery.registerPeer(userId, username);
        
        initialized = true;
        System.out.println("[P2P Service] Initialized for user: " + username);
    }
    
    /**
     * Connect to a peer by user ID.
     * First tries broadcast discovery, then falls back to database lookup.
     * @param peerId Target peer's user ID
     * @return CompletableFuture that completes with chat session
     */
    public CompletableFuture<P2PChatSession> connectToPeer(Long peerId) {
        // Check if already connected
        if (connectionManager.isConnectedTo(peerId)) {
            return CompletableFuture.completedFuture(connectionManager.getSession(peerId));
        }
        
        // First try broadcast discovery (for LAN peers)
        P2PBroadcastDiscovery broadcastDiscovery = connectionManager.getBroadcastDiscovery();
        if (broadcastDiscovery != null) {
            P2PBroadcastDiscovery.DiscoveredPeer discoveredPeer = broadcastDiscovery.getDiscoveredPeer(peerId);
            if (discoveredPeer != null && !discoveredPeer.isExpired()) {
                System.out.println("[P2P Service] Connecting via broadcast discovery: " + 
                                  discoveredPeer.ipAddress + ":" + discoveredPeer.port);
                return connectionManager.connectToPeer(
                    discoveredPeer.ipAddress,
                    discoveredPeer.port,
                    peerId
                );
            }
        }
        
        // Fall back to database lookup
        Optional<PeerInfo> peerInfoOpt = peerDiscovery.findPeer(peerId);
        
        if (peerInfoOpt.isEmpty() || !peerInfoOpt.get().isOnline()) {
            CompletableFuture<P2PChatSession> future = new CompletableFuture<>();
            future.completeExceptionally(new IOException("Peer not found or not online: " + peerId));
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
     * Connect to a peer by IP address and port (manual connection).
     * @param host Peer's IP address
     * @param port Peer's port number
     * @return CompletableFuture that completes with chat session
     */
    public CompletableFuture<P2PChatSession> connectToPeerManual(String host, int port) {
        // For manual connection, we use 0 as placeholder peer ID
        // The actual peer ID will be determined during handshake
        return connectionManager.connectToPeer(host, port, 0L);
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
     * Get list of discovered peers from broadcast (LAN discovery).
     * This is the primary method for finding peers on the same network.
     * @return List of discovered peers as PeerInfo objects
     */
    public List<PeerInfo> getDiscoveredPeers() {
        List<PeerInfo> peers = new ArrayList<>();
        
        P2PBroadcastDiscovery broadcastDiscovery = connectionManager.getBroadcastDiscovery();
        if (broadcastDiscovery != null) {
            List<P2PBroadcastDiscovery.DiscoveredPeer> discovered = 
                broadcastDiscovery.getDiscoveredPeersList();
            
            for (P2PBroadcastDiscovery.DiscoveredPeer peer : discovered) {
                if (!peer.isExpired()) {
                    PeerInfo info = new PeerInfo(peer.userId, peer.username, 
                                                 peer.ipAddress, peer.port);
                    info.setOnline(true);
                    peers.add(info);
                }
            }
        }
        
        return peers;
    }
    
    /**
     * Get list of online peers (from database - legacy method).
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
     * Add a peer discovery listener.
     * @param listener The listener to add
     */
    public void addDiscoveryListener(P2PConnectionManager.PeerDiscoveryListener listener) {
        connectionManager.addDiscoveryListener(listener);
    }
    
    /**
     * Remove a peer discovery listener.
     * @param listener The listener to remove
     */
    public void removeDiscoveryListener(P2PConnectionManager.PeerDiscoveryListener listener) {
        connectionManager.removeDiscoveryListener(listener);
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
     * Force announce presence on the network.
     * Useful to immediately notify other peers of our presence.
     */
    public void forceAnnounce() {
        connectionManager.forceAnnounce();
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
        
        // Unregister peer from database
        Long userId = UserService.getCurrentUser().getId();
        peerDiscovery.unregisterPeer(userId);
        
        // Shutdown connection manager (also stops broadcast discovery)
        connectionManager.shutdown();
        
        initialized = false;
        System.out.println("[P2P Service] Shutdown complete");
    }
}
