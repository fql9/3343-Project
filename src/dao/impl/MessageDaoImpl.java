package dao.impl;

import config.DatabaseConfig;
import dao.MessageDao;
import model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDaoImpl implements MessageDao {

    @Override
    public Message findById(Long id) {
        String sql = "SELECT * FROM messages WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) { e.printStackTrace(); }

        return null;
    }

    @Override
    public List<Message> findConversation(Long user1, Long user2) {
        List<Message> list = new ArrayList<>();

        String sql = """
            SELECT * FROM messages 
            WHERE (from_user_id=? AND to_user_id=?) 
               OR (from_user_id=? AND to_user_id=?)
            ORDER BY created_time ASC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, user1);
            ps.setLong(2, user2);
            ps.setLong(3, user2);
            ps.setLong(4, user1);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public List<Message> findInbox(Long userId) {
        List<Message> list = new ArrayList<>();

        String sql = "SELECT * FROM messages WHERE to_user_id=? ORDER BY created_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public void save(Message message) {
        String sql = """
            INSERT INTO messages(from_user_id, to_user_id, content, created_time, read)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, message.getFromUserId());
            ps.setLong(2, message.getToUserId());
            ps.setString(3, message.getContent());
            ps.setString(4, message.getCreatedTime());
            ps.setInt(5, message.isRead() ? 1 : 0);

            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void markRead(Long id) {
        String sql = "UPDATE messages SET read=1 WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM messages WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setId(rs.getLong("id"));
        msg.setFromUserId(rs.getLong("from_user_id"));
        msg.setToUserId(rs.getLong("to_user_id"));
        msg.setContent(rs.getString("content"));
        msg.setCreatedTime(rs.getString("created_time"));
        msg.setRead(rs.getInt("read") == 1);
        return msg;
    }
}
