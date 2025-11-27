package dao.impl;

import config.DatabaseConfig;
import dao.OrderDao;
import model.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Data Access Object implementation.
 * Provides database operations for Order entity.
 */
public class OrderDaoImpl implements OrderDao {

    @Override
    public void save(Order order) {
        String sql = "INSERT INTO orders (order_no, buyer_id, seller_id, item_id, amount, status, shipping_address, created_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, order.getOrderNo());
            stmt.setLong(2, order.getBuyerId());
            stmt.setLong(3, order.getSellerId());
            stmt.setLong(4, order.getItemId());
            stmt.setDouble(5, order.getAmount());
            stmt.setString(6, order.getStatus());
            stmt.setString(7, order.getShippingAddress());
            stmt.setString(8, order.getCreatedTime());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Order order) {
        String sql = "UPDATE orders SET status = ?, shipping_address = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, order.getStatus());
            stmt.setString(2, order.getShippingAddress());
            stmt.setLong(3, order.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Order findById(Long id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Order findByOrderNo(String orderNo) {
        String sql = "SELECT * FROM orders WHERE order_no = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, orderNo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Order> findByBuyerId(Long buyerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE buyer_id = ? ORDER BY created_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, buyerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public List<Order> findBySellerId(Long sellerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE seller_id = ? ORDER BY created_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sellerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderNo(rs.getString("order_no"));
        order.setBuyerId(rs.getLong("buyer_id"));
        order.setSellerId(rs.getLong("seller_id"));
        order.setItemId(rs.getLong("item_id"));
        order.setAmount(rs.getDouble("amount"));
        order.setStatus(rs.getString("status"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setCreatedTime(rs.getString("created_time"));
        return order;
    }
}
