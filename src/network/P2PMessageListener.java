package network;

/**
 * Interface for listening to P2P message events.
 * Implement this interface to receive callbacks when P2P events occur.
 */
public interface P2PMessageListener {
    
    /**
     * Called when a new message is received.
     * @param message The received P2P message
     */
    void onMessageReceived(P2PMessage message);
    
    /**
     * Called when a peer connects.
     * @param peerId The ID of the connected peer
     * @param peerName The username of the connected peer
     */
    void onPeerConnected(Long peerId, String peerName);
    
    /**
     * Called when a peer disconnects.
     * @param peerId The ID of the disconnected peer
     */
    void onPeerDisconnected(Long peerId);
    
    /**
     * Called when an error occurs.
     * @param error The error message
     */
    void onError(String error);
    
    /**
     * Called when peer starts or stops typing.
     * @param peerId The ID of the peer
     * @param isTyping Whether the peer is typing
     */
    default void onTypingStatusChanged(Long peerId, boolean isTyping) {
        // Default empty implementation
    }
}

