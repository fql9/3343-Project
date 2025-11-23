package dao.impl;

import config.DatabaseConfig;
import dao.UserDao;
import model.User;
import model.UserRole;

import java.sql.*;
import java.util.ArrayList; 
import java.util.List;

public class UserDaoImpl implements UserDao {

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash, email, role, active) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, user.isActive() ? 1 : 0);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(User user) {
        String sql = """
                UPDATE users 
                SET username = ?, password_hash = ?, email = ?, role = ?, active = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, user.isActive() ? 1 : 0);
            ps.setLong(6, user.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------
    //  将 ResultSet → User 对象
    // ------------------------
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();

        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setEmail(rs.getString("email"));
        
        String roleStr = rs.getString("role");
        try {
            u.setRole(UserRole.valueOf(roleStr));
        } catch (IllegalArgumentException | NullPointerException e) {
            // 如果数据库中的角色字符串在枚举中找不到（例如 "USER"），则使用默认值防止崩溃
            System.err.println("Warning: Invalid role '" + roleStr + "' found in database for user " + u.getUsername());
            // 使用枚举中的第一个值作为默认值（防止 null 导致后续空指针）
            if (UserRole.values().length > 0) {
                u.setRole(UserRole.values()[0]); 
            }
        }
        
        u.setActive(rs.getInt("active") == 1);

        return u;
    }
}
