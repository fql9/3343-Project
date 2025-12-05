package service;

import service.OrderService;
import service.UserService;
import service.ItemService;
import service.NotificationService;
import config.DatabaseConfig;
import model.Item;
import model.Notification;
import model.Order;
import model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;
    private UserService userService;
    private ItemService itemService;
    private NotificationService notificationService;
    
    private Long testBuyerId;
    private Long testSellerId;
    private Long testItemId;
    private Long testOperatorId;

    @BeforeAll
    static void setupDatabase() {
        // Ensure database is initialized
        DatabaseConfig.initDatabase();
    }

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
        userService = new UserService();
        itemService = new ItemService();
        notificationService = new NotificationService();
        
        // Clean up test data
        cleanupTestData();
        
        // Create test buyer
        userService.register("order_test_buyer", "password123", "buyer@test.com", UserRole.BUYER);
        userService.login("order_test_buyer", "password123");
        testBuyerId = UserService.getCurrentUser().getId();
        userService.logout();
        
        // Create test seller
        userService.register("order_test_seller", "password123", "seller@test.com", UserRole.SELLER);
        userService.login("order_test_seller", "password123");
        testSellerId = UserService.getCurrentUser().getId();
        
        // Create test item
        itemService.publishItem(testSellerId, "Test Item", "Test Description", 100.0, "Electronics", null);
        List<Item> items = itemService.getItemsBySeller(testSellerId);
        testItemId = items.get(0).getId();
        userService.logout();
        
        // Create test operator (for permission tests)
        userService.register("order_test_operator", "password123", "operator@test.com", UserRole.BUYER);
        userService.login("order_test_operator", "password123");
        testOperatorId = UserService.getCurrentUser().getId();
        userService.logout();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try (java.sql.Connection conn = DatabaseConfig.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            // Delete test orders
            stmt.executeUpdate("DELETE FROM orders WHERE buyer_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'order_test_%')");
            stmt.executeUpdate("DELETE FROM orders WHERE seller_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'order_test_%')");
            // Delete test notifications
            stmt.executeUpdate("DELETE FROM notifications WHERE user_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'order_test_%')");
            // Delete test items
            stmt.executeUpdate("DELETE FROM items WHERE seller_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'order_test_%')");
            // Delete test users
            stmt.executeUpdate("DELETE FROM users WHERE username LIKE 'order_test_%'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateOrder_Success() {
        // Act
        String result = orderService.createOrder(testBuyerId, testItemId, "123 Test Street");

        // Assert
        assertNull(result, "Order creation should succeed");
        
        // Verify order was created
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        assertFalse(orders.isEmpty(), "Buyer should have orders");
        assertEquals(testBuyerId, orders.get(0).getBuyerId());
        assertEquals(testSellerId, orders.get(0).getSellerId());
        assertEquals(testItemId, orders.get(0).getItemId());
        assertEquals(100.0, orders.get(0).getAmount(), 0.01);
        assertEquals("PAID", orders.get(0).getStatus());
        assertEquals("123 Test Street", orders.get(0).getShippingAddress());
        assertNotNull(orders.get(0).getOrderNo());
        assertNotNull(orders.get(0).getCreatedTime());
        
        // Verify item is marked as inactive
        Item item = itemService.getItemById(testItemId);
        assertFalse(item.isActive(), "Item should be marked as sold");
    }

    @Test
    void testCreateOrder_ItemNotFound() {
        // Act
        String result = orderService.createOrder(testBuyerId, 99999L, "123 Test Street");

        // Assert
        assertEquals("Item not found", result);
        
        // Verify no order was created
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        assertTrue(orders.isEmpty());
    }

    @Test
    void testCreateOrder_ItemNotActive() {
        // Arrange - Create an order to make item inactive
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");

        // Create another buyer
        userService.register("order_test_buyer2", "password123", "buyer2@test.com", UserRole.BUYER);
        userService.login("order_test_buyer2", "password123");
        Long buyer2Id = UserService.getCurrentUser().getId();
        userService.logout();

        // Act - Try to order the same item again
        String result = orderService.createOrder(buyer2Id, testItemId, "456 Test Avenue");

        // Assert
        assertEquals("Item is no longer available", result);
    }

    @Test
    void testCreateOrder_CannotBuyOwnItem() {
        // Act - Seller tries to buy their own item
        String result = orderService.createOrder(testSellerId, testItemId, "123 Test Street");

        // Assert
        assertEquals("Cannot buy your own item", result);
        
        // Verify no order was created
        List<Order> orders = orderService.getOrdersByBuyer(testSellerId);
        assertTrue(orders.isEmpty());
    }

    @Test
    void testCreateOrder_EmptyShippingAddress() {
        // Act
        String result = orderService.createOrder(testBuyerId, testItemId, "");

        // Assert
        assertEquals("Shipping address cannot be empty", result);
        
        // Verify no order was created
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        assertTrue(orders.isEmpty());
    }

    @Test
    void testCreateOrder_NullShippingAddress() {
        // Act
        String result = orderService.createOrder(testBuyerId, testItemId, null);

        // Assert
        assertEquals("Shipping address cannot be empty", result);
        
        // Verify no order was created
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        assertTrue(orders.isEmpty());
    }

    @Test
    void testCreateOrder_WhitespaceShippingAddress() {
        // Act
        String result = orderService.createOrder(testBuyerId, testItemId, "   ");

        // Assert
        assertEquals("Shipping address cannot be empty", result);
    }

    @Test
    void testCreateOrder_NotificationCreated() {
        // Act
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");

        // Assert - Verify seller received notification
        List<Notification> notifications = notificationService.getUnreadNotifications(testSellerId);
        assertFalse(notifications.isEmpty(), "Seller should receive notification");
    }

    @Test
    void testUpdateOrderStatus_ShipOrder_Success() {
        // Arrange - Create an order
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();

        // Act - Seller ships the order
        String result = orderService.updateOrderStatus(orderId, "SHIPPED", testSellerId);

        // Assert
        assertNull(result, "Status update should succeed");
        
        Order updatedOrder = orderService.getOrdersByBuyer(testBuyerId).get(0);
        assertEquals("SHIPPED", updatedOrder.getStatus());
    }

    @Test
    void testUpdateOrderStatus_CompleteOrder_Success() {
        // Arrange - Create and ship an order
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();
        orderService.updateOrderStatus(orderId, "SHIPPED", testSellerId);

        // Act - Buyer completes the order
        String result = orderService.updateOrderStatus(orderId, "COMPLETED", testBuyerId);

        // Assert
        assertNull(result, "Status update should succeed");
        
        Order updatedOrder = orderService.getOrdersByBuyer(testBuyerId).get(0);
        assertEquals("COMPLETED", updatedOrder.getStatus());
    }

    @Test
    void testUpdateOrderStatus_CancelOrder_Success() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();

        // Act - Buyer cancels the order
        String result = orderService.updateOrderStatus(orderId, "CANCELLED", testBuyerId);

        // Assert
        assertNull(result, "Cancellation should succeed");
        
        Order updatedOrder = orderService.getOrdersByBuyer(testBuyerId).get(0);
        assertEquals("CANCELLED", updatedOrder.getStatus());
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        // Act
        String result = orderService.updateOrderStatus(99999L, "SHIPPED", testSellerId);

        // Assert
        assertEquals("Order not found", result);
    }

    @Test
    void testUpdateOrderStatus_NoChange() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();

        // Act - Try to set same status
        String result = orderService.updateOrderStatus(orderId, "PAID", testSellerId);

        // Assert
        assertNull(result, "No change should return null");
    }

    @Test
    void testUpdateOrderStatus_PermissionDenied() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();

        // Act - Unrelated user tries to update
        String result = orderService.updateOrderStatus(orderId, "SHIPPED", testOperatorId);

        // Assert
        assertEquals("Permission denied", result);
    }

    @Test
    void testUpdateOrderStatus_OnlySellerCanShip() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();

        // Act - Buyer tries to ship
        String result = orderService.updateOrderStatus(orderId, "SHIPPED", testBuyerId);

        // Assert
        assertEquals("Only seller can ship the order", result);
    }

    @Test
    void testUpdateOrderStatus_OnlyBuyerCanComplete() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();
        orderService.updateOrderStatus(orderId, "SHIPPED", testSellerId);

        // Act - Seller tries to complete
        String result = orderService.updateOrderStatus(orderId, "COMPLETED", testSellerId);

        // Assert
        assertEquals("Only buyer can complete the order", result);
    }

    @Test
    void testUpdateOrderStatus_MustBePaidBeforeShipping() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();
        
        // Manually change status to CANCELLED
        orderService.updateOrderStatus(orderId, "CANCELLED", testBuyerId);

        // Act - Try to ship cancelled order
        String result = orderService.updateOrderStatus(orderId, "SHIPPED", testSellerId);

        // Assert
        assertEquals("Order must be PAID before shipping", result);
    }

    @Test
    void testUpdateOrderStatus_MustBeShippedBeforeCompletion() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();

        // Act - Try to complete without shipping
        String result = orderService.updateOrderStatus(orderId, "COMPLETED", testBuyerId);

        // Assert
        assertEquals("Order must be SHIPPED before completion", result);
    }

    @Test
    void testUpdateOrderStatus_CannotCancelCompleted() {
        // Arrange - Create, ship and complete order
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();
        orderService.updateOrderStatus(orderId, "SHIPPED", testSellerId);
        orderService.updateOrderStatus(orderId, "COMPLETED", testBuyerId);

        // Act - Try to cancel completed order
        String result = orderService.updateOrderStatus(orderId, "CANCELLED", testBuyerId);

        // Assert
        assertEquals("Cannot cancel completed order", result);
    }

    @Test
    void testUpdateOrderStatus_NotificationSent() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();
        
        int initialUnreadCount = notificationService.getUnreadNotifications(testBuyerId).size();

        // Act - Seller ships the order
        orderService.updateOrderStatus(orderId, "SHIPPED", testSellerId);

        // Assert - Buyer should receive notification
        int newUnreadCount = notificationService.getUnreadNotifications(testBuyerId).size();
        assertTrue(newUnreadCount > initialUnreadCount, "Buyer should receive status update notification");
    }

    @Test
    void testGetOrdersByBuyer() {
        // Arrange - Create multiple orders
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        
        // Create another item and order
        userService.login("order_test_seller", "password123");
        itemService.publishItem(testSellerId, "Test Item 2", "Description", 50.0, "Electronics", null);
        List<Item> items = itemService.getItemsBySeller(testSellerId);
        Long item2Id = items.get(items.size() - 1).getId();
        userService.logout();
        
        orderService.createOrder(testBuyerId, item2Id, "456 Test Avenue");

        // Act
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);

        // Assert
        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    @Test
    void testGetOrdersByBuyer_NoOrders() {
        // Act
        List<Order> orders = orderService.getOrdersByBuyer(testOperatorId);

        // Assert
        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    @Test
    void testGetOrdersBySeller() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        
        // Create another buyer and order
        userService.register("order_test_buyer2", "password123", "buyer2@test.com", UserRole.BUYER);
        userService.login("order_test_buyer2", "password123");
        Long buyer2Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("order_test_seller", "password123");
        itemService.publishItem(testSellerId, "Test Item 2", "Description", 50.0, "Electronics", null);
        List<Item> items = itemService.getItemsBySeller(testSellerId);
        Long item2Id = items.get(items.size() - 1).getId();
        userService.logout();
        
        orderService.createOrder(buyer2Id, item2Id, "456 Test Avenue");

        // Act
        List<Order> orders = orderService.getOrdersBySeller(testSellerId);

        // Assert
        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    @Test
    void testGetOrdersBySeller_NoOrders() {
        // Act
        List<Order> orders = orderService.getOrdersBySeller(testOperatorId);

        // Assert
        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    @Test
    void testCreateOrder_WithLongAddress() {
        // Arrange
        String longAddress = "Very long address ".repeat(20);

        // Act
        String result = orderService.createOrder(testBuyerId, testItemId, longAddress);

        // Assert
        assertNull(result);
        
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        assertEquals(longAddress, orders.get(0).getShippingAddress());
    }

    @Test
    void testCreateOrder_WithSpecialCharacters() {
        // Arrange
        String specialAddress = "123 Test St., Apt #5 @Building-A (North Wing) [Floor 2]";

        // Act
        String result = orderService.createOrder(testBuyerId, testItemId, specialAddress);

        // Assert
        assertNull(result);
        
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        assertEquals(specialAddress, orders.get(0).getShippingAddress());
    }

    @Test
    void testCreateOrder_WithUnicode() {
        // Arrange
        String unicodeAddress = "北京市朝阳区 东京都渋谷区 서울특별시";

        // Act
        String result = orderService.createOrder(testBuyerId, testItemId, unicodeAddress);

        // Assert
        assertNull(result);
        
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        assertEquals(unicodeAddress, orders.get(0).getShippingAddress());
    }

    @Test
    void testOrderTimestamp() {
        // Arrange & Act
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);

        // Assert
        assertNotNull(orders.get(0).getCreatedTime());
        assertFalse(orders.get(0).getCreatedTime().isEmpty());
        // Verify timestamp format (yyyy-MM-dd HH:mm:ss)
        assertTrue(orders.get(0).getCreatedTime().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testOrderNumberUniqueness() {
        // Arrange - Create multiple orders
        userService.login("order_test_seller", "password123");
        itemService.publishItem(testSellerId, "Test Item 2", "Description", 50.0, "Electronics", null);
        List<Item> items = itemService.getItemsBySeller(testSellerId);
        Long item2Id = items.get(items.size() - 1).getId();
        userService.logout();

        // Act
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        orderService.createOrder(testBuyerId, item2Id, "456 Test Avenue");

        // Assert
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        assertEquals(2, orders.size());
        assertNotEquals(orders.get(0).getOrderNo(), orders.get(1).getOrderNo(), 
                "Order numbers should be unique");
    }

    @Test
    void testFullOrderLifecycle() {
        // Arrange & Act
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();

        // Assert initial state
        assertEquals("PAID", orders.get(0).getStatus());

        // Ship order
        orderService.updateOrderStatus(orderId, "SHIPPED", testSellerId);
        orders = orderService.getOrdersByBuyer(testBuyerId);
        assertEquals("SHIPPED", orders.get(0).getStatus());

        // Complete order
        orderService.updateOrderStatus(orderId, "COMPLETED", testBuyerId);
        orders = orderService.getOrdersByBuyer(testBuyerId);
        assertEquals("COMPLETED", orders.get(0).getStatus());
    }

    @Test
    void testSellerCanCancelBeforeShipping() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();

        // Act - Seller cancels order
        String result = orderService.updateOrderStatus(orderId, "CANCELLED", testSellerId);

        // Assert
        assertNull(result, "Seller should be able to cancel PAID order");
        
        Order cancelledOrder = orderService.getOrdersByBuyer(testBuyerId).get(0);
        assertEquals("CANCELLED", cancelledOrder.getStatus());
    }

    @Test
    void testBuyerCanCancelShippedOrder() {
        // Arrange
        orderService.createOrder(testBuyerId, testItemId, "123 Test Street");
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        Long orderId = orders.get(0).getId();
        orderService.updateOrderStatus(orderId, "SHIPPED", testSellerId);

        // Act - Buyer cancels shipped order
        String result = orderService.updateOrderStatus(orderId, "CANCELLED", testBuyerId);

        // Assert
        assertNull(result, "Buyer should be able to cancel SHIPPED order");
        
        Order cancelledOrder = orderService.getOrdersByBuyer(testBuyerId).get(0);
        assertEquals("CANCELLED", cancelledOrder.getStatus());
    }
}
