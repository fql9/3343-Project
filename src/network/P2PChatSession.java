package network;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Represents an active P2P chat session between two users.
 * Handles sending and receiving messages for a single peer connection.
 */
public class P2PChatSession {
    
    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private final Long peerId;
    private final String peerName;
    private final boolean isInitiator;
    private final P2PConnectionManager connectionManager;
    
    private volatile boolean active;
    private ExecutorService receiveExecutor;
    private ScheduledExecutorService heartbeatExecutor;
    
    private static final int HEARTBEAT_INTERVAL = 30; // seconds
    
    /**
     * Constructor for P2P Chat Session.
     * @param socket Socket connection to peer
     * @param inputStream Input stream for receiving messages
     * @param outputStream Output stream for sending messages
     * @param peerId Peer's user ID
     * @param peerName Peer's username
     * @param isInitiator Whether this side initiated the connection
     * @param connectionManager Connection manager reference
     */
    public P2PChatSession(Socket socket, ObjectInputStream inputStream, 
                          ObjectOutputStream outputStream, Long peerId, 
                          String peerName, boolean isInitiator,
                          P2PConnectionManager connectionManager) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.peerId = peerId;
        this.peerName = peerName;
        this.isInitiator = isInitiator;
        this.connectionManager = connectionManager;
        this.active = true;
        this.receiveExecutor = Executors.newSingleThreadExecutor();
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    
    /**
     * Start receiving messages from the peer.
     */
    public void startReceiving() {
        // Notify connection manager about new peer connection
        connectionManager.notifyPeerConnected(peerId, peerName);
        
        // Start heartbeat
        startHeartbeat();
        
        // Start receive loop
        receiveExecutor.submit(this::receiveLoop);
    }
    
    /**
     * Message receiving loop.
     */
    private void receiveLoop() {
        while (active && !socket.isClosed()) {
            try {
                P2PMessage message = (P2PMessage) inputStream.readObject();
                handleMessage(message);
            } catch (SocketException | EOFException e) {
                if (active) {
                    System.out.println("[P2P Session] Connection closed by peer: " + peerName);
                    close();
                }
                break;
            } catch (IOException | ClassNotFoundException e) {
                if (active) {
                    System.err.println("[P2P Session] Receive error: " + e.getMessage());
                    close();
                }
                break;
            }
        }
    }
    
    /**
     * Handle received message based on its type.
     * @param message The received message
     */
    private void handleMessage(P2PMessage message) {
        switch (message.getType()) {
            case CHAT:
                connectionManager.notifyMessageReceived(message);
                break;
            case HEARTBEAT:
                // Respond to heartbeat with ACK
                sendAck(message.getMessageId());
                break;
            case ACK:
                // Heartbeat acknowledged, connection is alive
                break;
            case TYPING:
                boolean isTyping = "1".equals(message.getContent());
                connectionManager.notifyTypingStatus(peerId, isTyping);
                break;
            case DISCONNECT:
                System.out.println("[P2P Session] Peer requested disconnect: " + peerName);
                close();
                break;
            default:
                System.out.println("[P2P Session] Unknown message type: " + message.getType());
        }
    }
    
    /**
     * Send a chat message to the peer.
     * @param content Message content
     * @return true if message was sent successfully
     */
    public boolean sendMessage(String content) {
        if (!active) {
            return false;
        }
        
        Long senderId = connectionManager.getCurrentUserId();
        String senderName = connectionManager.getCurrentUsername();
        
        P2PMessage message = P2PMessage.createChatMessage(senderId, senderName, peerId, content);
        return sendP2PMessage(message);
    }
    
    /**
     * Send a P2P message object.
     * @param message The message to send
     * @return true if message was sent successfully
     */
    public synchronized boolean sendP2PMessage(P2PMessage message) {
        if (!active || socket.isClosed()) {
            return false;
        }
        
        try {
            outputStream.writeObject(message);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            System.err.println("[P2P Session] Send error: " + e.getMessage());
            close();
            return false;
        }
    }
    
    /**
     * Send typing indicator to peer.
     * @param isTyping Whether user is currently typing
     */
    public void sendTypingIndicator(boolean isTyping) {
        if (!active) return;
        
        P2PMessage typingMsg = P2PMessage.createTypingIndicator(
            connectionManager.getCurrentUserId(), isTyping
        );
        sendP2PMessage(typingMsg);
    }
    
    /**
     * Send acknowledgment for a message.
     * @param messageId The ID of the message to acknowledge
     */
    private void sendAck(String messageId) {
        P2PMessage ack = P2PMessage.createAck(connectionManager.getCurrentUserId(), messageId);
        sendP2PMessage(ack);
    }
    
    /**
     * Start sending heartbeat messages periodically.
     */
    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (active) {
                P2PMessage heartbeat = P2PMessage.createHeartbeat(
                    connectionManager.getCurrentUserId()
                );
                if (!sendP2PMessage(heartbeat)) {
                    close();
                }
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }
    
    /**
     * Close the chat session gracefully.
     */
    public void close() {
        if (!active) {
            return;
        }
        
        active = false;
        
        // Send disconnect message
        try {
            P2PMessage disconnect = P2PMessage.createDisconnect(
                connectionManager.getCurrentUserId()
            );
            outputStream.writeObject(disconnect);
            outputStream.flush();
        } catch (IOException e) {
            // Ignore, we're closing anyway
        }
        
        // Shutdown executors
        heartbeatExecutor.shutdown();
        receiveExecutor.shutdown();
        
        // Close streams and socket
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            // Ignore close errors
        }
        
        // Notify connection manager
        connectionManager.unregisterSession(peerId);
        connectionManager.notifyPeerDisconnected(peerId);
        
        System.out.println("[P2P Session] Closed session with: " + peerName);
    }
    
    /**
     * Check if the session is active.
     * @return true if session is active
     */
    public boolean isActive() {
        return active && !socket.isClosed();
    }
    
    /**
     * Get the peer's user ID.
     * @return Peer ID
     */
    public Long getPeerId() {
        return peerId;
    }
    
    /**
     * Get the peer's username.
     * @return Peer username
     */
    public String getPeerName() {
        return peerName;
    }
    
    /**
     * Check if this side initiated the connection.
     * @return true if this side initiated
     */
    public boolean isInitiator() {
        return isInitiator;
    }
    
    /**
     * Get peer's remote address.
     * @return Remote address string
     */
    public String getPeerAddress() {
        return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }
}

