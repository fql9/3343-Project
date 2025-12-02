package model;

/**
 * PeerInfo entity class representing a P2P user's network information.
 * Stores IP address and port for establishing direct connections.
 */
public class PeerInfo {
    private Long userId;
    private String username;
    private String ipAddress;
    private int port;
    private boolean online;
    private String lastActiveTime;

    public PeerInfo() {}

    public PeerInfo(Long userId, String username, String ipAddress, int port) {
        this.userId = userId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;
        this.online = true;
    }

    // Getter & Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(String lastActiveTime) { this.lastActiveTime = lastActiveTime; }

    @Override
    public String toString() {
        return "PeerInfo{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", online=" + online +
                '}';
    }
}

