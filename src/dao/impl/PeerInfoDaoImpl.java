package dao.impl;

import config.DatabaseConfig;
import dao.PeerInfoDao;
import model.PeerInfo;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * PeerInfo Data Access Object implementation.
 * Provides database operations for PeerInfo entity.
 */
public class PeerInfoDaoImpl implements PeerInfoDao {

    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void saveOrUpdate(PeerInfo peerInfo) {
        // Try update first, if no rows affected then insert
        String updateSql = """
            UPDATE peer_info 
            SET username=?, ip_address=?, port=?, online=1, last_active_time=?
            WHERE user_id=?
        """;
        
        String insertSql = """
            INSERT INTO peer_info(user_id, username, ip_address, port, online, last_active_time)
            VALUES (?, ?, ?, ?, 1, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection()) {
            String currentTime = LocalDateTime.now().format(DATE_FORMATTER);
            
            // Try update
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, peerInfo.getUsername());
                ps.setString(2, peerInfo.getIpAddress());
                ps.setInt(3, peerInfo.getPort());
                ps.setString(4, currentTime);
                ps.setLong(5, peerInfo.getUserId());
                
                int rows = ps.executeUpdate();
                if (rows > 0) return; // Update successful
            }
            
            // Insert if update didn't affect any rows
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setLong(1, peerInfo.getUserId());
                ps.setString(2, peerInfo.getUsername());
                ps.setString(3, peerInfo.getIpAddress());
                ps.setInt(4, peerInfo.getPort());
                ps.setString(5, currentTime);
                ps.executeUpdate();
            }
            
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    @Override
    public PeerInfo findByUserId(Long userId) {
        String sql = "SELECT * FROM peer_info WHERE user_id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) { 
            e.printStackTrace(); 
        }

        return null;
    }

    @Override
    public List<PeerInfo> findAllOnline() {
        List<PeerInfo> list = new ArrayList<>();
        String sql = "SELECT * FROM peer_info WHERE online=1 ORDER BY last_active_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { 
            e.printStackTrace(); 
        }

        return list;
    }

    @Override
    public List<PeerInfo> findAllOnlineExcept(Long excludeUserId) {
        List<PeerInfo> list = new ArrayList<>();
        String sql = "SELECT * FROM peer_info WHERE online=1 AND user_id!=? ORDER BY last_active_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, excludeUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { 
            e.printStackTrace(); 
        }

        return list;
    }

    @Override
    public void setOffline(Long userId) {
        String sql = "UPDATE peer_info SET online=0 WHERE user_id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    @Override
    public void setAllOffline() {
        String sql = "UPDATE peer_info SET online=0";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    @Override
    public void delete(Long userId) {
        String sql = "DELETE FROM peer_info WHERE user_id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    private PeerInfo mapRow(ResultSet rs) throws SQLException {
        PeerInfo info = new PeerInfo();
        info.setUserId(rs.getLong("user_id"));
        info.setUsername(rs.getString("username"));
        info.setIpAddress(rs.getString("ip_address"));
        info.setPort(rs.getInt("port"));
        info.setOnline(rs.getInt("online") == 1);
        info.setLastActiveTime(rs.getString("last_active_time"));
        return info;
    }
}

