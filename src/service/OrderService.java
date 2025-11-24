package service;

import dao.ItemDao;
import dao.OrderDao;
import dao.impl.ItemDaoImpl;
import dao.impl.OrderDaoImpl;
import model.Item;
import model.Order;
import util.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class OrderService {

    private final OrderDao orderDao;
    private final ItemDao itemDao;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OrderService() {
        this.orderDao = new OrderDaoImpl();
        this.itemDao = new ItemDaoImpl();
    }

    /**
     * Create a new order
     */
    public String createOrder(Long buyerId, Long itemId, String shippingAddress) {
        Item item = itemDao.findById(itemId);
        if (item == null) {
            return "Item not found";
        }
        if (!item.isActive()) {
            return "Item is no longer available";
        }
        if (item.getSellerId().equals(buyerId)) {
            return "Cannot buy your own item";
        }
        if (!ValidationUtils.isNotEmpty(shippingAddress)) {
            return "Shipping address cannot be empty";
        }

        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString());
        order.setBuyerId(buyerId);
        order.setSellerId(item.getSellerId());
        order.setItemId(itemId);
        order.setAmount(item.getPrice());
        order.setStatus("PAID"); // For simplicity, assume immediate payment
        order.setShippingAddress(shippingAddress);
        order.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));

        orderDao.save(order);

        // Mark item as sold (inactive)
        item.setActive(false);
        itemDao.update(item);

        return null; // Success
    }

    public List<Order> getOrdersByBuyer(Long buyerId) {
        return orderDao.findByBuyerId(buyerId);
    }

    public List<Order> getOrdersBySeller(Long sellerId) {
        return orderDao.findBySellerId(sellerId);
    }
}
