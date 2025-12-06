package integration.service;

import integration.IntegrationTestBase;
import model.Order;
import model.UserRole;
import service.ItemService;
import service.OrderService;
import service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderService 补充测试 - 边界条件和异常分支
 * 目标: 提升分支覆盖率
 */
class OrderServiceCoverageTest extends IntegrationTestBase {

    private OrderService orderService;
    private UserService userService;
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
        userService = new UserService();
        itemService = new ItemService();
    }

    @Test
    void testCreateOrderWithNonExistentItem() {
        // 创建买家
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        userService.logout();
        
        // 尝试购买不存在的商品
        String result = orderService.createOrder(buyerId, 99999L, "Address");
        assertEquals("Item not found", result);
    }

    @Test
    void testCreateOrderWithInactiveItem() {
        // 创建卖家和买家
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        
        // 下架商品
        itemService.deactivateItem(itemId);
        userService.logout();
        
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        
        // 尝试购买已下架商品
        String result = orderService.createOrder(buyerId, itemId, "Address");
        assertEquals("Item is no longer available", result);
        
        userService.logout();
    }

    @Test
    void testCreateOrderBuyOwnItem() {
        // 创建卖家
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        
        // 卖家尝试购买自己的商品
        String result = orderService.createOrder(sellerId, itemId, "Address");
        assertEquals("Cannot buy your own item", result);
        
        userService.logout();
    }

    @Test
    void testCreateOrderWithEmptyAddress() {
        // 创建卖家和买家
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        userService.logout();
        
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        
        // 空地址
        String result = orderService.createOrder(buyerId, itemId, "");
        assertEquals("Shipping address cannot be empty", result);
        
        userService.logout();
    }

    @Test
    void testCreateOrderWithNullAddress() {
        // 创建卖家和买家
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        userService.logout();
        
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        
        // null地址
        String result = orderService.createOrder(buyerId, itemId, null);
        assertEquals("Shipping address cannot be empty", result);
        
        userService.logout();
    }

    @Test
    void testUpdateOrderStatusToSameStatus() {
        // 创建订单
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        userService.logout();
        
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        orderService.createOrder(buyerId, itemId, "Address");
        Long orderId = orderService.getOrdersByBuyer(buyerId).get(0).getId();
        userService.logout();
        
        // 尝试更新为相同状态
        userService.login("seller", "pass123");
        String result = orderService.updateOrderStatus(orderId, "PAID", sellerId);
        assertNull(result, "相同状态应该直接返回null");
        userService.logout();
    }

    @Test
    void testUpdateOrderStatusWithNonExistentOrder() {
        userService.register("user", "pass123", "user@test.com", UserRole.BUYER);
        userService.login("user", "pass123");
        Long userId = UserService.getCurrentUser().getId();
        
        String result = orderService.updateOrderStatus(99999L, "SHIPPED", userId);
        assertEquals("Order not found", result);
        
        userService.logout();
    }

    @Test
    void testUpdateOrderStatusWithWrongUser() {
        // 创建订单
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        userService.register("other", "pass123", "other@test.com", UserRole.BUYER);
        
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        userService.logout();
        
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        orderService.createOrder(buyerId, itemId, "Address");
        Long orderId = orderService.getOrdersByBuyer(buyerId).get(0).getId();
        userService.logout();
        
        // 其他用户尝试更新订单
        userService.login("other", "pass123");
        Long otherId = UserService.getCurrentUser().getId();
        String result = orderService.updateOrderStatus(orderId, "SHIPPED", otherId);
        assertEquals("Permission denied", result);
        userService.logout();
    }

    @Test
    void testBuyerCannotShipOrder() {
        // 创建订单
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        userService.logout();
        
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        orderService.createOrder(buyerId, itemId, "Address");
        Long orderId = orderService.getOrdersByBuyer(buyerId).get(0).getId();
        
        // 买家尝试发货
        String result = orderService.updateOrderStatus(orderId, "SHIPPED", buyerId);
        assertEquals("Only seller can ship the order", result);
        userService.logout();
    }

    @Test
    void testSellerCannotCompleteOrder() {
        // 创建并发货订单
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        userService.logout();
        
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        orderService.createOrder(buyerId, itemId, "Address");
        Long orderId = orderService.getOrdersByBuyer(buyerId).get(0).getId();
        userService.logout();
        
        // 卖家发货
        userService.login("seller", "pass123");
        orderService.updateOrderStatus(orderId, "SHIPPED", sellerId);
        
        // 卖家尝试完成订单
        String result = orderService.updateOrderStatus(orderId, "COMPLETED", sellerId);
        assertEquals("Only buyer can complete the order", result);
        userService.logout();
    }

    @Test
    void testCannotShipNonPaidOrder() {
        // 这个测试需要先创建一个非PAID状态的订单
        // 由于createOrder会自动设为PAID，我们测试取消后不能发货
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        userService.logout();
        
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        orderService.createOrder(buyerId, itemId, "Address");
        Long orderId = orderService.getOrdersByBuyer(buyerId).get(0).getId();
        
        // 取消订单
        orderService.updateOrderStatus(orderId, "CANCELLED", buyerId);
        userService.logout();
        
        // 卖家尝试发货已取消的订单
        userService.login("seller", "pass123");
        String result = orderService.updateOrderStatus(orderId, "SHIPPED", sellerId);
        assertEquals("Order must be PAID before shipping", result);
        userService.logout();
    }

    @Test
    void testCannotCompleteNonShippedOrder() {
        // 创建订单但不发货
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        userService.logout();
        
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        orderService.createOrder(buyerId, itemId, "Address");
        Long orderId = orderService.getOrdersByBuyer(buyerId).get(0).getId();
        
        // 买家直接尝试完成订单（跳过发货）
        String result = orderService.updateOrderStatus(orderId, "COMPLETED", buyerId);
        assertEquals("Order must be SHIPPED before completion", result);
        userService.logout();
    }

    @Test
    void testCannotCancelCompletedOrder() {
        // 创建并完成订单
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item", "Desc", 100.0, "Cat", null);
        Long itemId = itemService.getItemsBySeller(sellerId).get(0).getId();
        userService.logout();
        
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        orderService.createOrder(buyerId, itemId, "Address");
        Long orderId = orderService.getOrdersByBuyer(buyerId).get(0).getId();
        userService.logout();
        
        // 发货
        userService.login("seller", "pass123");
        orderService.updateOrderStatus(orderId, "SHIPPED", sellerId);
        userService.logout();
        
        // 完成
        userService.login("buyer", "pass123");
        orderService.updateOrderStatus(orderId, "COMPLETED", buyerId);
        
        // 尝试取消已完成的订单
        String result = orderService.updateOrderStatus(orderId, "CANCELLED", buyerId);
        assertEquals("Cannot cancel completed order", result);
        userService.logout();
    }

    @Test
    void testGetOrdersByBuyerWithNoOrders() {
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        
        List<Order> orders = orderService.getOrdersByBuyer(buyerId);
        assertTrue(orders.isEmpty());
        
        userService.logout();
    }

    @Test
    void testGetOrdersBySellerWithNoOrders() {
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        
        List<Order> orders = orderService.getOrdersBySeller(sellerId);
        assertTrue(orders.isEmpty());
        
        userService.logout();
    }
}

