package network;

/**
 * Configuration class for P2P network settings.
 * Contains all configurable parameters for P2P communication.
 */
public class P2PConfig {
    
    // Default port range starts at 50000
    private int basePort = 50000;
    
    // Maximum connections allowed
    private int maxConnections = 50;
    
    // Connection timeout in milliseconds
    private int connectionTimeout = 10000;
    
    // Heartbeat interval in seconds
    private int heartbeatInterval = 30;
    
    // Message queue size
    private int messageQueueSize = 100;
    
    // Enable message persistence (save to database)
    private boolean persistMessages = true;
    
    // Enable encryption (placeholder for future implementation)
    private boolean enableEncryption = false;
    
    /**
     * Default constructor with default values.
     */
    public P2PConfig() {
        // Use default values
    }
    
    /**
     * Get base port for P2P server.
     * @return Base port number
     */
    public int getBasePort() {
        return basePort;
    }
    
    /**
     * Set base port for P2P server.
     * @param basePort Base port number
     */
    public void setBasePort(int basePort) {
        this.basePort = basePort;
    }
    
    /**
     * Get maximum allowed connections.
     * @return Maximum connections
     */
    public int getMaxConnections() {
        return maxConnections;
    }
    
    /**
     * Set maximum allowed connections.
     * @param maxConnections Maximum connections
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    /**
     * Get connection timeout in milliseconds.
     * @return Connection timeout
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    /**
     * Set connection timeout in milliseconds.
     * @param connectionTimeout Connection timeout
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    /**
     * Get heartbeat interval in seconds.
     * @return Heartbeat interval
     */
    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }
    
    /**
     * Set heartbeat interval in seconds.
     * @param heartbeatInterval Heartbeat interval
     */
    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
    
    /**
     * Get message queue size.
     * @return Message queue size
     */
    public int getMessageQueueSize() {
        return messageQueueSize;
    }
    
    /**
     * Set message queue size.
     * @param messageQueueSize Message queue size
     */
    public void setMessageQueueSize(int messageQueueSize) {
        this.messageQueueSize = messageQueueSize;
    }
    
    /**
     * Check if message persistence is enabled.
     * @return true if messages should be persisted
     */
    public boolean isPersistMessages() {
        return persistMessages;
    }
    
    /**
     * Set message persistence flag.
     * @param persistMessages Whether to persist messages
     */
    public void setPersistMessages(boolean persistMessages) {
        this.persistMessages = persistMessages;
    }
    
    /**
     * Check if encryption is enabled.
     * @return true if encryption is enabled
     */
    public boolean isEnableEncryption() {
        return enableEncryption;
    }
    
    /**
     * Set encryption flag.
     * @param enableEncryption Whether to enable encryption
     */
    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }
    
    /**
     * Calculate the port for a specific user.
     * @param userId User's ID
     * @return Port number for the user
     */
    public int getPortForUser(Long userId) {
        return basePort + (userId.intValue() % 1000);
    }
}

