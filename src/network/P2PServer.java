package network;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * P2P Server class that listens for incoming peer connections.
 * Each user runs a P2P server to accept connection requests from other users.
 */
public class P2PServer {
    
    private ServerSocket serverSocket;
    private final int port;
    private final Long userId;
    private final String username;
    private volatile boolean running;
    private ExecutorService executorService;
    private P2PConnectionManager connectionManager;
    
    /**
     * Constructor for P2P Server.
     * @param port Port number to listen on
     * @param userId Current user's ID
     * @param username Current user's username
     * @param connectionManager Connection manager instance
     */
    public P2PServer(int port, Long userId, String username, P2PConnectionManager connectionManager) {
        this.port = port;
        this.userId = userId;
        this.username = username;
        this.connectionManager = connectionManager;
        this.running = false;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * Start the P2P server and begin listening for connections.
     * @throws IOException if server socket cannot be created
     */
    public void start() throws IOException {
        if (running) {
            return;
        }
        
        serverSocket = new ServerSocket(port);
        running = true;
        
        System.out.println("[P2P Server] Started on port " + port);
        
        // Start accepting connections in a new thread
        executorService.submit(this::acceptConnections);
    }
    
    /**
     * Accept incoming connections loop.
     */
    private void acceptConnections() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[P2P Server] New connection from: " + 
                    clientSocket.getInetAddress().getHostAddress());
                
                // Handle the new connection in a separate thread
                executorService.submit(() -> handleConnection(clientSocket));
                
            } catch (SocketException e) {
                if (running) {
                    System.err.println("[P2P Server] Socket error: " + e.getMessage());
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("[P2P Server] Accept error: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handle an incoming connection.
     * @param socket The client socket
     */
    private void handleConnection(Socket socket) {
        String clientIP = socket.getInetAddress().getHostAddress();
        
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            
            // Read handshake message
            P2PMessage handshake = (P2PMessage) in.readObject();
            
            if (handshake.getType() == P2PMessage.MessageType.HANDSHAKE) {
                Long peerId = handshake.getSenderId();
                String peerName = handshake.getSenderName();
                
                System.out.println("[P2P Server] Handshake received from: " + peerName + " (" + clientIP + ")");
                
                // Check if already connected to this peer
                if (connectionManager.isConnectedToByName(peerName)) {
                    System.out.println("[P2P Server] Already connected to " + peerName + ", rejecting");
                    socket.close();
                    return;
                }
                
                // Send response handshake
                P2PMessage response = P2PMessage.createHandshake(userId, username);
                out.writeObject(response);
                out.flush();
                
                // Create a chat session and register with connection manager
                P2PChatSession session = new P2PChatSession(
                    socket, in, out, peerId, peerName, false, connectionManager
                );
                
                // Try to register - might fail if duplicate
                boolean registered = connectionManager.registerSession(peerId, peerName, session);
                if (!registered) {
                    System.out.println("[P2P Server] Failed to register session for " + peerName);
                    socket.close();
                    return;
                }
                
                // Start receiving messages
                session.startReceiving();
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[P2P Server] Connection handling error: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ex) {
                // Ignore close errors
            }
        }
    }
    
    /**
     * Stop the P2P server.
     */
    public void stop() {
        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[P2P Server] Error closing server socket: " + e.getMessage());
        }
        
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        
        System.out.println("[P2P Server] Stopped");
    }
    
    /**
     * Check if the server is running.
     * @return true if server is running
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Get the server port.
     * @return Port number
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Get the local IP address.
     * @return Local IP address string
     */
    public String getLocalAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}

