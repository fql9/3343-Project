package network;

import dao.PeerInfoDao;
import dao.impl.PeerInfoDaoImpl;
import model.PeerInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for P2P peer discovery.
 * Manages peer information registration and lookup for connection establishment.
 */
public class P2PPeerDiscovery {
    
    private final PeerInfoDao peerInfoDao;
    private final P2PConfig config;
    
    /**
     * Constructor for P2P Peer Discovery.
     * @param config P2P configuration
     */
    public P2PPeerDiscovery(P2PConfig config) {
        this.peerInfoDao = new PeerInfoDaoImpl();
        this.config = config;
    }
    
    /**
     * Register current user's peer information.
     * @param userId User's ID
     * @param username User's username
     * @return true if registration successful
     */
    public boolean registerPeer(Long userId, String username) {
        try {
            String ipAddress = getLocalIPAddress();
            int port = config.getPortForUser(userId);
            
            PeerInfo peerInfo = new PeerInfo(userId, username, ipAddress, port);
            peerInfoDao.saveOrUpdate(peerInfo);
            
            System.out.println("[Peer Discovery] Registered peer: " + username + 
                              " at " + ipAddress + ":" + port);
            return true;
            
        } catch (Exception e) {
            System.err.println("[Peer Discovery] Failed to register peer: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Unregister peer (mark as offline).
     * @param userId User's ID
     */
    public void unregisterPeer(Long userId) {
        peerInfoDao.setOffline(userId);
        System.out.println("[Peer Discovery] Unregistered peer: " + userId);
    }
    
    /**
     * Find peer information by user ID.
     * @param userId User's ID to look up
     * @return Optional containing PeerInfo if found
     */
    public Optional<PeerInfo> findPeer(Long userId) {
        PeerInfo peerInfo = peerInfoDao.findByUserId(userId);
        return Optional.ofNullable(peerInfo);
    }
    
    /**
     * Get all online peers.
     * @return List of online peer information
     */
    public List<PeerInfo> getOnlinePeers() {
        return peerInfoDao.findAllOnline();
    }
    
    /**
     * Get all online peers except specified user.
     * @param excludeUserId User ID to exclude
     * @return List of online peers
     */
    public List<PeerInfo> getOnlinePeersExcept(Long excludeUserId) {
        return peerInfoDao.findAllOnlineExcept(excludeUserId);
    }
    
    /**
     * Get local IP address.
     * @return Local IP address string
     */
    private String getLocalIPAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
    
    /**
     * Check if a peer is online and reachable.
     * @param peerInfo Peer information
     * @param timeout Connection timeout
     * @return true if peer is reachable
     */
    public boolean isPeerReachable(PeerInfo peerInfo, int timeout) {
        if (peerInfo == null || !peerInfo.isOnline()) {
            return false;
        }
        
        // Try to connect to check if peer is reachable
        P2PClient tempClient = new P2PClient(0L, "", null);
        boolean reachable = tempClient.isPeerReachable(
            peerInfo.getIpAddress(), 
            peerInfo.getPort(), 
            timeout
        );
        tempClient.shutdown();
        
        return reachable;
    }
    
    /**
     * Set all peers offline (for cleanup on startup).
     */
    public void setAllPeersOffline() {
        peerInfoDao.setAllOffline();
    }
}
