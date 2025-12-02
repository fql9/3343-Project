package dao;

import model.PeerInfo;
import java.util.List;

/**
 * PeerInfo Data Access Object interface.
 * Defines operations for managing P2P peer information.
 */
public interface PeerInfoDao {
    
    /**
     * Register or update peer information
     * @param peerInfo The peer information to save
     */
    void saveOrUpdate(PeerInfo peerInfo);
    
    /**
     * Find peer by user ID
     * @param userId The user ID
     * @return PeerInfo or null if not found
     */
    PeerInfo findByUserId(Long userId);
    
    /**
     * Get all online peers
     * @return List of online peers
     */
    List<PeerInfo> findAllOnline();
    
    /**
     * Get all online peers except specified user
     * @param excludeUserId User ID to exclude
     * @return List of online peers
     */
    List<PeerInfo> findAllOnlineExcept(Long excludeUserId);
    
    /**
     * Set peer offline
     * @param userId The user ID to set offline
     */
    void setOffline(Long userId);
    
    /**
     * Set all peers offline (for cleanup on startup)
     */
    void setAllOffline();
    
    /**
     * Delete peer info
     * @param userId The user ID
     */
    void delete(Long userId);
}

