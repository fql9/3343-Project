package network;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * UDP Broadcast-based peer discovery service for LAN environments.
 * Enables automatic discovery of peers on the same local network
 * without requiring a central server.
 * 
 * Uses username as unique identifier since multiple users might have same userId
 * in different local databases.
 */
public class P2PBroadcastDiscovery {
    
    // UDP broadcast port for peer discovery
    private static final int DISCOVERY_PORT = 50999;
    
    // Broadcast interval in seconds
    private static final int BROADCAST_INTERVAL = 2;
    
    // Peer timeout in seconds (remove if not seen for this long)
    private static final int PEER_TIMEOUT = 10;
    
    // Message prefix for discovery packets
    private static final String MESSAGE_PREFIX = "P2P_DISCOVER:";
    
    private DatagramSocket broadcastSocket;
    private DatagramSocket listenerSocket;
    private volatile boolean running;
    
    private Long userId;
    private String username;
    private int serverPort;
    private Set<String> localIPAddresses;
    
    // Key: "ip:username" for uniqueness across different databases
    private final Map<String, DiscoveredPeer> discoveredPeers;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService listenerExecutor;
    
    private P2PConnectionManager connectionManager;
    
    // Cache of broadcast addresses to send to
    private List<InetAddress> broadcastAddresses;
    
    /**
     * Represents a discovered peer with timestamp.
     */
    public static class DiscoveredPeer {
        public final Long userId;
        public final String username;
        public final String ipAddress;
        public final int port;
        public long lastSeen;
        
        public DiscoveredPeer(Long userId, String username, String ipAddress, int port) {
            this.userId = userId;
            this.username = username;
            this.ipAddress = ipAddress;
            this.port = port;
            this.lastSeen = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - lastSeen > PEER_TIMEOUT * 1000;
        }
        
        public void updateLastSeen() {
            this.lastSeen = System.currentTimeMillis();
        }
        
        public String getKey() {
            return ipAddress + ":" + username;
        }
    }
    
    /**
     * Constructor for broadcast discovery service.
     */
    public P2PBroadcastDiscovery() {
        this.discoveredPeers = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.listenerExecutor = Executors.newSingleThreadExecutor();
        this.running = false;
        this.broadcastAddresses = new ArrayList<>();
        this.localIPAddresses = new HashSet<>();
    }
    
    /**
     * Start the broadcast discovery service.
     * @param userId Current user's ID
     * @param username Current user's username
     * @param serverPort P2P server port
     * @param connectionManager Connection manager reference
     * @throws IOException if sockets cannot be created
     */
    public void start(Long userId, String username, int serverPort, 
                      P2PConnectionManager connectionManager) throws IOException {
        if (running) {
            return;
        }
        
        this.userId = userId;
        this.username = username;
        this.serverPort = serverPort;
        this.connectionManager = connectionManager;
        
        // Collect local IP addresses to identify our own broadcasts
        collectLocalIPAddresses();
        
        // Collect all broadcast addresses from network interfaces
        collectBroadcastAddresses();
        
        // Create broadcast socket (for sending)
        broadcastSocket = new DatagramSocket();
        broadcastSocket.setBroadcast(true);
        broadcastSocket.setReuseAddress(true);
        
        // Create listener socket - bind to wildcard address to receive from any interface
        listenerSocket = new DatagramSocket(null);
        listenerSocket.setReuseAddress(true);
        listenerSocket.setBroadcast(true);
        listenerSocket.bind(new InetSocketAddress("0.0.0.0", DISCOVERY_PORT));
        
        running = true;
        
        // Start listening for broadcasts
        listenerExecutor.submit(this::listenForBroadcasts);
        
        // Start periodic broadcasting (more frequent initially)
        scheduler.scheduleAtFixedRate(
            this::broadcastPresence,
            0,
            BROADCAST_INTERVAL,
            TimeUnit.SECONDS
        );
        
        // Start peer cleanup task
        scheduler.scheduleAtFixedRate(
            this::cleanupExpiredPeers,
            PEER_TIMEOUT,
            PEER_TIMEOUT / 2,
            TimeUnit.SECONDS
        );
        
        System.out.println("[Broadcast Discovery] Started on port " + DISCOVERY_PORT);
        System.out.println("[Broadcast Discovery] Local IPs: " + localIPAddresses);
        System.out.println("[Broadcast Discovery] Broadcast addresses: " + broadcastAddresses);
    }
    
    /**
     * Collect all local IP addresses to identify our own broadcasts.
     */
    private void collectLocalIPAddresses() {
        localIPAddresses.clear();
        localIPAddresses.add("127.0.0.1");
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                if (!networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        localIPAddresses.add(addr.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Broadcast Discovery] Error collecting local IPs: " + e.getMessage());
        }
    }
    
    /**
     * Collect broadcast addresses from all network interfaces.
     */
    private void collectBroadcastAddresses() {
        broadcastAddresses.clear();
        
        try {
            // Add global broadcast
            broadcastAddresses.add(InetAddress.getByName("255.255.255.255"));
            
            // Enumerate all network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // Skip loopback, down, or virtual interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                // Get all interface addresses
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast != null && !broadcastAddresses.contains(broadcast)) {
                        broadcastAddresses.add(broadcast);
                        System.out.println("[Broadcast Discovery] Found broadcast address: " + 
                                          broadcast.getHostAddress() + " on " + networkInterface.getDisplayName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Broadcast Discovery] Error collecting broadcast addresses: " + e.getMessage());
        }
        
        // Ensure we have at least the global broadcast
        if (broadcastAddresses.isEmpty()) {
            try {
                broadcastAddresses.add(InetAddress.getByName("255.255.255.255"));
            } catch (UnknownHostException e) {
                // Should never happen
            }
        }
    }
    
    /**
     * Broadcast presence to all peers on the network.
     */
    private void broadcastPresence() {
        if (!running || broadcastSocket == null || broadcastSocket.isClosed()) {
            return;
        }
        
        try {
            // Create discovery message: PREFIX userId:username:port
            String message = MESSAGE_PREFIX + userId + ":" + username + ":" + serverPort;
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            
            // Send to all collected broadcast addresses
            for (InetAddress broadcastAddr : broadcastAddresses) {
                try {
                    DatagramPacket packet = new DatagramPacket(
                        data, 
                        data.length,
                        broadcastAddr,
                        DISCOVERY_PORT
                    );
                    broadcastSocket.send(packet);
                } catch (IOException e) {
                    // Some broadcast addresses might fail, that's okay
                }
            }
            
        } catch (Exception e) {
            System.err.println("[Broadcast Discovery] Broadcast error: " + e.getMessage());
        }
    }
    
    /**
     * Listen for broadcast messages from other peers.
     */
    private void listenForBroadcasts() {
        byte[] buffer = new byte[1024];
        
        System.out.println("[Broadcast Discovery] Listening for peer broadcasts...");
        
        while (running && !listenerSocket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                listenerSocket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength(), 
                                           StandardCharsets.UTF_8);
                String senderIP = packet.getAddress().getHostAddress();
                
                if (message.startsWith(MESSAGE_PREFIX)) {
                    handleDiscoveryMessage(message, senderIP);
                }
                
            } catch (SocketException e) {
                if (running) {
                    System.err.println("[Broadcast Discovery] Socket error: " + e.getMessage());
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("[Broadcast Discovery] Listen error: " + e.getMessage());
                }
            }
        }
        
        System.out.println("[Broadcast Discovery] Listener stopped");
    }
    
    /**
     * Handle a received discovery message.
     * @param message The discovery message
     * @param senderIP IP address of the sender
     */
    private void handleDiscoveryMessage(String message, String senderIP) {
        try {
            // Parse message: PREFIX userId:username:port
            String content = message.substring(MESSAGE_PREFIX.length());
            String[] parts = content.split(":");
            
            if (parts.length < 3) {
                return;
            }
            
            Long peerId = Long.parseLong(parts[0]);
            String peerName = parts[1];
            int peerPort = Integer.parseInt(parts[2]);
            
            // Check if this is our own broadcast by checking:
            // 1. Same username AND from local IP
            // 2. OR same IP address
            boolean isOwnBroadcast = localIPAddresses.contains(senderIP) && peerName.equals(this.username);
            
            if (isOwnBroadcast) {
                // This is our own broadcast, ignore it
                return;
            }
            
            // Use IP + username as unique key (since userId might be same in different DBs)
            String peerKey = senderIP + ":" + peerName;
            
            // Check if this is a new peer or update existing
            DiscoveredPeer existing = discoveredPeers.get(peerKey);
            
            if (existing != null) {
                // Update last seen time
                existing.updateLastSeen();
            } else {
                // New peer discovered!
                DiscoveredPeer newPeer = new DiscoveredPeer(peerId, peerName, senderIP, peerPort);
                discoveredPeers.put(peerKey, newPeer);
                
                System.out.println("[Broadcast Discovery] *** NEW PEER: " + peerName + 
                                  " at " + senderIP + ":" + peerPort + " ***");
                
                // Notify connection manager about new peer
                if (connectionManager != null) {
                    connectionManager.notifyPeerDiscovered(peerId, peerName, senderIP, peerPort);
                }
            }
            
        } catch (NumberFormatException e) {
            System.err.println("[Broadcast Discovery] Invalid message format");
        }
    }
    
    /**
     * Remove expired peers from the discovered list.
     */
    private void cleanupExpiredPeers() {
        discoveredPeers.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                System.out.println("[Broadcast Discovery] Peer expired: " + 
                                  entry.getValue().username);
                return true;
            }
            return false;
        });
    }
    
    /**
     * Get all currently discovered peers.
     * @return Map of peer key to peer information
     */
    public Map<String, DiscoveredPeer> getDiscoveredPeers() {
        // Return a copy to prevent modification
        return new ConcurrentHashMap<>(discoveredPeers);
    }
    
    /**
     * Get all discovered peers as a list.
     * @return List of discovered peers
     */
    public List<DiscoveredPeer> getDiscoveredPeersList() {
        return new ArrayList<>(discoveredPeers.values());
    }
    
    /**
     * Get a specific discovered peer by ID.
     * @param peerId Peer's user ID
     * @return DiscoveredPeer or null if not found
     */
    public DiscoveredPeer getDiscoveredPeer(Long peerId) {
        // Search by userId (may return first match if multiple)
        for (DiscoveredPeer peer : discoveredPeers.values()) {
            if (peer.userId.equals(peerId)) {
                return peer;
            }
        }
        return null;
    }
    
    /**
     * Get a discovered peer by username.
     * @param username Peer's username
     * @return DiscoveredPeer or null if not found
     */
    public DiscoveredPeer getDiscoveredPeerByUsername(String username) {
        for (DiscoveredPeer peer : discoveredPeers.values()) {
            if (peer.username.equals(username)) {
                return peer;
            }
        }
        return null;
    }
    
    /**
     * Check if a peer has been discovered.
     * @param peerId Peer's user ID
     * @return true if peer is in discovered list
     */
    public boolean isPeerDiscovered(Long peerId) {
        DiscoveredPeer peer = getDiscoveredPeer(peerId);
        return peer != null && !peer.isExpired();
    }
    
    /**
     * Force a broadcast to announce presence immediately.
     */
    public void forceAnnounce() {
        System.out.println("[Broadcast Discovery] Force announcing presence...");
        // Refresh local IPs and broadcast addresses in case network changed
        collectLocalIPAddresses();
        collectBroadcastAddresses();
        broadcastPresence();
    }
    
    /**
     * Stop the broadcast discovery service.
     */
    public void stop() {
        running = false;
        
        // Close sockets
        if (broadcastSocket != null && !broadcastSocket.isClosed()) {
            broadcastSocket.close();
        }
        
        if (listenerSocket != null && !listenerSocket.isClosed()) {
            listenerSocket.close();
        }
        
        // Shutdown executors
        scheduler.shutdown();
        listenerExecutor.shutdown();
        
        try {
            scheduler.awaitTermination(2, TimeUnit.SECONDS);
            listenerExecutor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            listenerExecutor.shutdownNow();
        }
        
        discoveredPeers.clear();
        
        System.out.println("[Broadcast Discovery] Stopped");
    }
    
    /**
     * Check if discovery service is running.
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Get the discovery port.
     * @return UDP discovery port number
     */
    public static int getDiscoveryPort() {
        return DISCOVERY_PORT;
    }
    
    /**
     * Get current broadcast addresses being used.
     * @return List of broadcast addresses
     */
    public List<String> getBroadcastAddressStrings() {
        List<String> result = new ArrayList<>();
        for (InetAddress addr : broadcastAddresses) {
            result.add(addr.getHostAddress());
        }
        return result;
    }
}
