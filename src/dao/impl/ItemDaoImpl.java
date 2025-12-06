package dao.impl;

import config.DatabaseConfig;
import dao.ItemDao;
import model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDaoImpl implements ItemDao {

    @Override
    public Item findById(Long id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Item> findAll() {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items ORDER BY created_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public List<Item> findBySellerId(Long sellerId) {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE seller_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sellerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public List<Item> search(String keyword) {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE title LIKE ? OR description LIKE ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public List<Item> searchItems(String keyword, Double minPrice, Double maxPrice, String category, String sortBy) {
        List<Item> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM items WHERE active = 1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }

        if (category != null && !category.trim().isEmpty() && !"All Categories".equals(category) && !"All".equals(category)) {
            sql.append(" AND category = ?");
            params.add(category);
        }

        if (sortBy != null) {
            switch (sortBy) {
                case "Price: Low to High":
                    sql.append(" ORDER BY price ASC");
                    break;
                case "Price: High to Low":
                    sql.append(" ORDER BY price DESC");
                    break;
                case "Newest First":
                default:
                    sql.append(" ORDER BY created_time DESC");
                    break;
            }
        } else {
            sql.append(" ORDER BY created_time DESC");
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void save(Item item) {
        String sql = """
            INSERT INTO items (seller_id, title, description, price, category, active, created_time, image_url)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, item.getSellerId());
            ps.setString(2, item.getTitle());
            // Handle null description
            if (item.getDescription() != null) {
                ps.setString(3, item.getDescription());
            } else {
                ps.setNull(3, java.sql.Types.VARCHAR);
            }
            ps.setDouble(4, item.getPrice());
            // Handle null category
            if (item.getCategory() != null) {
                ps.setString(5, item.getCategory());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }
            ps.setInt(6, item.isActive() ? 1 : 0);
            ps.setString(7, item.getCreatedTime());
            // Handle null imageUrl
            if (item.getImageUrl() != null && !item.getImageUrl().trim().isEmpty()) {
                ps.setString(8, item.getImageUrl());
            } else {
                ps.setNull(8, java.sql.Types.VARCHAR);
            }

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Failed to save item: No rows affected");
            }

        } catch (SQLException e) {
            System.err.println("SQL Error saving item: " + e.getMessage());
            System.err.println("Item details: sellerId=" + item.getSellerId() + 
                             ", title=" + item.getTitle() + 
                             ", price=" + item.getPrice());
            e.printStackTrace();
            throw new RuntimeException("Failed to save item: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Item item) {
        String sql = """
            UPDATE items SET
            seller_id=?, title=?, description=?, price=?, category=?, active=?, image_url=?
            WHERE id=?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, item.getSellerId());
            ps.setString(2, item.getTitle());
            ps.setString(3, item.getDescription());
            ps.setDouble(4, item.getPrice());
            ps.setString(5, item.getCategory());
            ps.setInt(6, item.isActive() ? 1 : 0);
            ps.setString(7, item.getImageUrl());
            ps.setLong(8, item.getId());

            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM items WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Item mapRow(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getLong("id"));
        item.setSellerId(rs.getLong("seller_id"));
        item.setTitle(rs.getString("title"));
        item.setDescription(rs.getString("description"));
        item.setPrice(rs.getDouble("price"));
        item.setCategory(rs.getString("category"));
        item.setActive(rs.getInt("active") == 1);
        item.setCreatedTime(rs.getString("created_time"));
        try {
            item.setImageUrl(rs.getString("image_url"));
        } catch (SQLException e) {
            // Ignore if column doesn't exist (backward compatibility)
        }
        return item;
    }
}
