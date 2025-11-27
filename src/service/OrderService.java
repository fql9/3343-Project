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

/**
 * Order service class - handles order-related business logic.
 * Provides order creation, status management, and query functions.
 */
public class OrderService {

    private final OrderDao orderDao;
    private final ItemDao itemDao;
    private final NotificationService notificationService;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OrderService() {
        this.orderDao = new OrderDaoImpl();
        this.itemDao = new ItemDaoImpl();
        this.notificationService = new NotificationService();
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

        // Notify Seller
        notificationService.createNotification(
            item.getSellerId(), 
            "New Order Received", 
            "You have a new order for item: " + item.getTitle()
        );

        return null; // Success
    }

    /**
     * Update order status
     */
    public String updateOrderStatus(Long orderId, String newStatus, Long operatorId) {
        Order order = orderDao.findById(orderId);
        if (order == null) {
            return "Order not found";
        }

        String oldStatus = order.getStatus();
        if (oldStatus.equals(newStatus)) {
            return null; // No change
        }

        // Validate transition (simplified)
        // PAID -> SHIPPED (Seller only)
        // SHIPPED -> COMPLETED (Buyer only)
        // * -> CANCELLED (Both, with restrictions)

        boolean isSeller = order.getSellerId().equals(operatorId);
        boolean isBuyer = order.getBuyerId().equals(operatorId);

        if (!isSeller && !isBuyer) {
            return "Permission denied";
        }

        if ("SHIPPED".equals(newStatus)) {
            if (!isSeller) return "Only seller can ship the order";
            if (!"PAID".equals(oldStatus)) return "Order must be PAID before shipping";
        } else if ("COMPLETED".equals(newStatus)) {
            if (!isBuyer) return "Only buyer can complete the order";
            if (!"SHIPPED".equals(oldStatus)) return "Order must be SHIPPED before completion";
        } else if ("CANCELLED".equals(newStatus)) {
            if ("COMPLETED".equals(oldStatus)) return "Cannot cancel completed order";
        }

        order.setStatus(newStatus);
        orderDao.update(order);

        // Notify the other party
        Long targetUserId = isSeller ? order.getBuyerId() : order.getSellerId();
        String title = "Order Status Updated";
        String content = "Order " + order.getOrderNo() + " status changed to " + newStatus;
        
        notificationService.createNotification(targetUserId, title, content);

        return null;
    }

    public List<Order> getOrdersByBuyer(Long buyerId) {
        return orderDao.findByBuyerId(buyerId);
    }

    public List<Order> getOrdersBySeller(Long sellerId) {
        return orderDao.findBySellerId(sellerId);
    }
}
