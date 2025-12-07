package integration.service;

import integration.IntegrationTestBase;
import service.UserService;
import service.OrderService;
import service.ItemService;
import config.DatabaseConfig;
import model.User;
import model.Item;
import model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceBranchCoverageTest extends IntegrationTestBase {

    private UserService userService;
    private OrderService orderService;
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        orderService = new OrderService();
        itemService = new ItemService();
        // 确保开始时没有登录
        userService.logout();
    }

    @AfterEach
    void tearDown() {
        userService.logout();
    }

    // ========== updateProfile 分支测试 ==========
    
    @Test
    void testUpdateProfile_Success() {
        // 注册并登录
        String regResult = userService.register("profile_user", "password123", "profile@test.com", UserRole.BUYER);
        assertNull(regResult, "Registration should succeed");
        
        String loginResult = userService.login("profile_user", "password123");
        assertNull(loginResult, "Login should succeed");
        assertTrue(UserService.isLoggedIn(), "Should be logged in");
        
        // 更新个人资料
        String result = userService.updateProfile("newemail@test.com", "My new bio", "http://avatar.com/img.png");
        
        assertNull(result, "Profile update should succeed");
        
        User currentUser = UserService.getCurrentUser();
        assertNotNull(currentUser, "Current user should not be null");
        assertEquals("newemail@test.com", currentUser.getEmail());
        assertEquals("My new bio", currentUser.getBio());
        assertEquals("http://avatar.com/img.png", currentUser.getAvatarUrl());
    }

    @Test
    void testUpdateProfile_NotLoggedIn() {
        // 确保未登录
        userService.logout();
        assertFalse(UserService.isLoggedIn());
        
        // 不登录直接更新
        String result = userService.updateProfile("newemail@test.com", "Bio", "http://avatar.com/img.png");
        
        assertEquals("Not logged in", result);
    }

    @Test
    void testUpdateProfile_InvalidEmail() {
        // 注册并登录
        String regResult = userService.register("invalid_email_user", "password123", "valid@test.com", UserRole.BUYER);
        assertNull(regResult, "Registration should succeed");
        
        String loginResult = userService.login("invalid_email_user", "password123");
        assertNull(loginResult, "Login should succeed");
        
        // 使用无效邮箱更新
        String result = userService.updateProfile("invalid-email-format", "Bio", "http://avatar.com/img.png");
        
        assertEquals("Invalid email format", result);
    }

    // ========== getTotalSalesAmount 分支测试 ==========
    
    @Test
    void testGetTotalSalesAmount_NotLoggedIn() {
        userService.logout();
        // 不登录时应返回 0
        double amount = userService.getTotalSalesAmount();
        assertEquals(0.0, amount);
    }

    @Test
    void testGetTotalSalesAmount_NoOrders() {
        // 注册并登录卖家
        String regResult = userService.register("sales_no_order", "password123", "sales_no@test.com", UserRole.SELLER);
        assertNull(regResult, "Registration should succeed");
        
        String loginResult = userService.login("sales_no_order", "password123");
        assertNull(loginResult, "Login should succeed");
        assertTrue(UserService.isLoggedIn(), "Should be logged in");
        
        // 没有订单，应该返回 0
        double amount = userService.getTotalSalesAmount();
        assertEquals(0.0, amount);
    }

    // ========== getTotalPurchaseAmount 分支测试 ==========
    
    @Test
    void testGetTotalPurchaseAmount_NotLoggedIn() {
        userService.logout();
        // 不登录时应返回 0
        double amount = userService.getTotalPurchaseAmount();
        assertEquals(0.0, amount);
    }

    @Test
    void testGetTotalPurchaseAmount_NoOrders() {
        // 注册并登录买家
        String regResult = userService.register("purchase_no_order", "password123", "purchase_no@test.com", UserRole.BUYER);
        assertNull(regResult, "Registration should succeed");
        
        String loginResult = userService.login("purchase_no_order", "password123");
        assertNull(loginResult, "Login should succeed");
        assertTrue(UserService.isLoggedIn(), "Should be logged in");
        
        // 没有订单，应该返回 0
        double amount = userService.getTotalPurchaseAmount();
        assertEquals(0.0, amount);
    }

    // ========== isLoggedIn 边界测试 ==========
    
    @Test
    void testIsLoggedIn_AfterSuccessfulLogin() {
        String regResult = userService.register("login_test_user", "password123", "login_test@test.com", UserRole.BUYER);
        assertNull(regResult, "Registration should succeed");
        
        String loginResult = userService.login("login_test_user", "password123");
        assertNull(loginResult, "Login should succeed");
        
        assertTrue(UserService.isLoggedIn(), "Should be logged in after successful login");
        assertNotNull(UserService.getCurrentUser(), "Current user should not be null");
    }

    @Test
    void testIsLoggedIn_BeforeLogin() {
        userService.logout();
        assertFalse(UserService.isLoggedIn());
        assertNull(UserService.getCurrentUser());
    }

    // ========== isAdmin 分支测试 ==========
    
    @Test
    void testIsAdmin_WithNullCurrentUser() {
        // 确保没有登录
        userService.logout();
        
        // currentUser 为 null 时应返回 false
        assertFalse(UserService.isAdmin());
    }

    @Test
    void testIsAdmin_WithBuyerRole() {
        String regResult = userService.register("buyer_admin_test", "password123", "buyer_admin@test.com", UserRole.BUYER);
        assertNull(regResult, "Registration should succeed");
        
        String loginResult = userService.login("buyer_admin_test", "password123");
        assertNull(loginResult, "Login should succeed");
        
        // Buyer 不是 Admin
        assertFalse(UserService.isAdmin());
    }

    @Test
    void testIsAdmin_WithSellerRole() {
        String regResult = userService.register("seller_admin_test", "password123", "seller_admin@test.com", UserRole.SELLER);
        assertNull(regResult, "Registration should succeed");
        
        String loginResult = userService.login("seller_admin_test", "password123");
        assertNull(loginResult, "Login should succeed");
        
        // Seller 不是 Admin
        assertFalse(UserService.isAdmin());
    }

    @Test
    void testIsAdmin_WithAdminRole() {
        String regResult = userService.register("admin_test_user", "password123", "admin_test@test.com", UserRole.ADMIN);
        assertNull(regResult, "Registration should succeed");
        
        String loginResult = userService.login("admin_test_user", "password123");
        assertNull(loginResult, "Login should succeed");
        
        // Admin 是 Admin
        assertTrue(UserService.isAdmin());
    }

    // ========== 订单金额测试 (简化版) ==========
    
    @Test
    void testSalesAndPurchaseWithOrders() {
        // 创建卖家
        String sellerReg = userService.register("seller_order_test", "password123", "seller_order@test.com", UserRole.SELLER);
        assertNull(sellerReg, "Seller registration should succeed");
        
        String sellerLogin = userService.login("seller_order_test", "password123");
        assertNull(sellerLogin, "Seller login should succeed");
        
        Long sellerId = UserService.getCurrentUser().getId();
        assertNotNull(sellerId, "Seller ID should not be null");
        
        // 发布商品
        String publishResult = itemService.publishItem(sellerId, "Order Test Item", "Description", 100.0, "Electronics", null);
        assertNull(publishResult, "Item publish should succeed");
        
        List<Item> items = itemService.getItemsBySeller(sellerId);
        assertFalse(items.isEmpty(), "Should have at least one item");
        Long itemId = items.get(0).getId();
        
        userService.logout();
        
        // 创建买家
        String buyerReg = userService.register("buyer_order_test", "password123", "buyer_order@test.com", UserRole.BUYER);
        assertNull(buyerReg, "Buyer registration should succeed");
        
        String buyerLogin = userService.login("buyer_order_test", "password123");
        assertNull(buyerLogin, "Buyer login should succeed");
        
        Long buyerId = UserService.getCurrentUser().getId();
        assertNotNull(buyerId, "Buyer ID should not be null");
        
        userService.logout();
        
        // 创建订单并设置为 COMPLETED
        orderService.createOrder(buyerId, itemId, "123 Test St");
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE orders SET status = 'COMPLETED' WHERE seller_id = " + sellerId);
        } catch (Exception e) {
            fail("Failed to update order status: " + e.getMessage());
        }
        
        // 检查卖家销售额
        userService.login("seller_order_test", "password123");
        double salesAmount = userService.getTotalSalesAmount();
        assertTrue(salesAmount > 0, "Sales amount should be > 0 for completed orders");
        userService.logout();
        
        // 检查买家购买额
        userService.login("buyer_order_test", "password123");
        double purchaseAmount = userService.getTotalPurchaseAmount();
        assertTrue(purchaseAmount > 0, "Purchase amount should be > 0 for completed orders");
    }
}
