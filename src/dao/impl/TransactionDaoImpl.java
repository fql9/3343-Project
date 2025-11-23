package dao.impl;

import config.DatabaseConfig;
import dao.TransactionDao;
import model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDaoImpl implements TransactionDao {

    @Override
    public Transaction findById(Long id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY created_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public List<Transaction> findByBuyerId(Long buyerId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE buyer_id = ? ORDER BY created_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, buyerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public List<Transaction> findBySellerId(Long sellerId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE seller_id = ? ORDER BY created_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sellerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public List<Transaction> findByItemId(Long itemId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE item_id = ? ORDER BY created_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itemId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    @Override
    public Transaction findByBuyerAndItem(Long buyerId, Long itemId) {
        String sql = "SELECT * FROM transactions WHERE buyer_id = ? AND item_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, buyerId);
            ps.setLong(2, itemId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void save(Transaction transaction) {
        String sql = """
            INSERT INTO transactions (item_id, buyer_id, seller_id, agreed_price, status, 
                                     delivery_method, shipping_address, tracking_number,
                                     item_received, item_verified, funds_released, 
                                     created_time, updated_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, transaction.getItemId());
            ps.setLong(2, transaction.getBuyerId());
            ps.setLong(3, transaction.getSellerId());
            ps.setDouble(4, transaction.getAgreedPrice());
            ps.setString(5, transaction.getStatus());
            ps.setString(6, transaction.getDeliveryMethod());
            ps.setString(7, transaction.getShippingAddress());
            ps.setString(8, transaction.getTrackingNumber());
            ps.setInt(9, transaction.isItemReceived() ? 1 : 0);
            ps.setInt(10, transaction.isItemVerified() ? 1 : 0);
            ps.setInt(11, transaction.isFundsReleased() ? 1 : 0);
            ps.setString(12, transaction.getCreatedTime());
            ps.setString(13, transaction.getUpdatedTime());

            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void update(Transaction transaction) {
        String sql = """
            UPDATE transactions SET
            item_id=?, buyer_id=?, seller_id=?, agreed_price=?, status=?,
            delivery_method=?, shipping_address=?, tracking_number=?,
            item_received=?, item_verified=?, funds_released=?,
            updated_time=?
            WHERE id=?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, transaction.getItemId());
            ps.setLong(2, transaction.getBuyerId());
            ps.setLong(3, transaction.getSellerId());
            ps.setDouble(4, transaction.getAgreedPrice());
            ps.setString(5, transaction.getStatus());
            ps.setString(6, transaction.getDeliveryMethod());
            ps.setString(7, transaction.getShippingAddress());
            ps.setString(8, transaction.getTrackingNumber());
            ps.setInt(9, transaction.isItemReceived() ? 1 : 0);
            ps.setInt(10, transaction.isItemVerified() ? 1 : 0);
            ps.setInt(11, transaction.isFundsReleased() ? 1 : 0);
            ps.setString(12, transaction.getUpdatedTime());
            ps.setLong(13, transaction.getId());

            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM transactions WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setItemId(rs.getLong("item_id"));
        transaction.setBuyerId(rs.getLong("buyer_id"));
        transaction.setSellerId(rs.getLong("seller_id"));
        transaction.setAgreedPrice(rs.getDouble("agreed_price"));
        transaction.setStatus(rs.getString("status"));
        transaction.setDeliveryMethod(rs.getString("delivery_method"));
        transaction.setShippingAddress(rs.getString("shipping_address"));
        transaction.setTrackingNumber(rs.getString("tracking_number"));
        transaction.setItemReceived(rs.getInt("item_received") == 1);
        transaction.setItemVerified(rs.getInt("item_verified") == 1);
        transaction.setFundsReleased(rs.getInt("funds_released") == 1);
        transaction.setCreatedTime(rs.getString("created_time"));
        transaction.setUpdatedTime(rs.getString("updated_time"));
        return transaction;
    }
}

