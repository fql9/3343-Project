package dao.impl;

import config.DatabaseConfig;
import dao.FavoriteDao;
import model.Favorite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDaoImpl implements FavoriteDao {

    @Override
    public Favorite findById(Long id) {
        String sql = "SELECT * FROM favorites WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Favorite> findByUserId(Long userId) {
        List<Favorite> list = new ArrayList<>();
        String sql = "SELECT * FROM favorites WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public List<Favorite> findByItemId(Long itemId) {
        List<Favorite> list = new ArrayList<>();
        String sql = "SELECT * FROM favorites WHERE item_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itemId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public boolean exists(Long userId, Long itemId) {
        String sql = "SELECT 1 FROM favorites WHERE user_id=? AND item_id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, itemId);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) { e.printStackTrace(); }

        return false;
    }

    @Override
    public void save(Favorite favorite) {
        String sql = """
            INSERT INTO favorites (user_id, item_id, created_time)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, favorite.getUserId());
            ps.setLong(2, favorite.getItemId());
            ps.setString(3, favorite.getCreatedTime());
            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM favorites WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void deleteByUserAndItem(Long userId, Long itemId) {
        String sql = "DELETE FROM favorites WHERE user_id=? AND item_id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, itemId);
            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Favorite mapRow(ResultSet rs) throws SQLException {
        Favorite fav = new Favorite();
        fav.setId(rs.getLong("id"));
        fav.setUserId(rs.getLong("user_id"));
        fav.setItemId(rs.getLong("item_id"));
        fav.setCreatedTime(rs.getString("created_time"));
        return fav;
    }
}
