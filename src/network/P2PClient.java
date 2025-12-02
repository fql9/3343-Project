package network;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * P2P Client class for initiating connections to other peers.
 * Used to connect to another user's P2P server for direct communication.
 */
public class P2PClient {
    
    private final Long userId;
    private final String username;
    private final P2PConnectionManager connectionManager;
    private final ExecutorService executorService;
    
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    
    /**
     * Constructor for P2P Client.
     * @param userId Current user's ID
     * @param username Current user's username
     * @param connectionManager Connection manager instance
     */
    public P2PClient(Long userId, String username, P2PConnectionManager connectionManager) {
        this.userId = userId;
        this.username = username;
        this.connectionManager = connectionManager;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * Connect to a peer asynchronously.
     * @param host Peer's host address
     * @param port Peer's port number
     * @param peerId Peer's user ID
     * @return CompletableFuture that completes when connection is established
     */
    public CompletableFuture<P2PChatSession> connectAsync(String host, int port, Long peerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return connect(host, port, peerId);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }
    
    /**
     * Connect to a peer synchronously.
     * @param host Peer's host address
     * @param port Peer's port number
     * @param peerId Peer's user ID (for verification)
     * @return Established chat session
     * @throws IOException if connection fails
     */
    public P2PChatSession connect(String host, int port, Long peerId) throws IOException {
        System.out.println("[P2P Client] Connecting to " + host + ":" + port);
        
        // Create socket with timeout
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT);
        socket.setSoTimeout(CONNECTION_TIMEOUT);
        
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            // Send handshake
            P2PMessage handshake = P2PMessage.createHandshake(userId, username);
            out.writeObject(handshake);
            out.flush();
            
            // Wait for response
            P2PMessage response = (P2PMessage) in.readObject();
            
            if (response.getType() == P2PMessage.MessageType.HANDSHAKE) {
                Long actualPeerId = response.getSenderId();
                String peerName = response.getSenderName();
                
                System.out.println("[P2P Client] Connected to peer: " + peerName);
                
                // Remove socket timeout for ongoing communication
                socket.setSoTimeout(0);
                
                // Create chat session
                P2PChatSession session = new P2PChatSession(
                    socket, in, out, actualPeerId, peerName, true, connectionManager
                );
                
                // Register with connection manager
                connectionManager.registerSession(actualPeerId, session);
                
                // Start receiving messages
                session.startReceiving();
                
                return session;
            } else {
                throw new IOException("Invalid handshake response");
            }
            
        } catch (ClassNotFoundException e) {
            socket.close();
            throw new IOException("Protocol error: " + e.getMessage());
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }
    
    /**
     * Check if a peer is reachable.
     * @param host Peer's host address
     * @param port Peer's port number
     * @param timeout Timeout in milliseconds
     * @return true if peer is reachable
     */
    public boolean isPeerReachable(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Shutdown the client executor service.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}

