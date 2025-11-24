package dao.impl;

import config.DatabaseConfig;
import dao.ReviewDao;
import model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDaoImpl implements ReviewDao {

    @Override
    public void save(Review review) {
        String sql = """
            INSERT INTO reviews (order_id, reviewer_id, reviewee_id, item_id, rating, comment, created_time)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, review.getOrderId());
            ps.setLong(2, review.getReviewerId());
            ps.setLong(3, review.getRevieweeId());
            ps.setLong(4, review.getItemId());
            ps.setInt(5, review.getRating());
            ps.setString(6, review.getComment());
            ps.setString(7, review.getCreatedTime());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Review> findBySellerId(Long sellerId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE reviewee_id = ? ORDER BY created_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Review findByOrderId(Long orderId) {
        String sql = "SELECT * FROM reviews WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public double getAverageRating(Long sellerId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE reviewee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setOrderId(rs.getLong("order_id"));
        review.setReviewerId(rs.getLong("reviewer_id"));
        review.setRevieweeId(rs.getLong("reviewee_id"));
        review.setItemId(rs.getLong("item_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setCreatedTime(rs.getString("created_time"));
        return review;
    }
}
