package dao;

import model.Order;
import java.util.List;

/**
 * Order Data Access Object interface.
 */
public interface OrderDao {
    void save(Order order);
    void update(Order order);
    Order findById(Long id);
    Order findByOrderNo(String orderNo);
    List<Order> findByBuyerId(Long buyerId);
    List<Order> findBySellerId(Long sellerId);
}
