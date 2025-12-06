package system;

import model.Item;
import model.Order;
import model.Review;
import model.UserRole;
import service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统测试 - 用户完整购物流程
 * 测试从用户注册、浏览商品、下单购买到评价的完整端到端流程
 */
@DisplayName("System Test: 用户完整购物流程")
public class UserPurchaseFlowSystemTest extends SystemTestBase {

    private UserService userService;
    private ItemService itemService;
    private OrderService orderService;
    private ReviewService reviewService;
    private FavoriteService favoriteService;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        itemService = new ItemService();
        orderService = new OrderService();
        reviewService = new ReviewService();
        favoriteService = new FavoriteService();
        notificationService = new NotificationService();
    }

    @Test
    @DisplayName("场景1: 完整的购物流程 - 从注册到购买和评价")
    void testCompleteUserPurchaseFlow() {
        // ============ Phase 1: 用户注册 ============
        // 1.1 卖家注册
        String sellerRegResult = userService.register(
            "seller_user", 
            "seller123", 
            "seller@test.com", 
            UserRole.SELLER
        );
        assertNull(sellerRegResult, "卖家注册应该成功");
        
        // 1.2 买家注册
        String buyerRegResult = userService.register(
            "buyer_user", 
            "buyer123", 
            "buyer@test.com", 
            UserRole.BUYER
        );
        assertNull(buyerRegResult, "买家注册应该成功");
        
        // ============ Phase 2: 卖家登录并发布商品 ============
        // 2.1 卖家登录
        String sellerLoginResult = userService.login("seller_user", "seller123");
        assertNull(sellerLoginResult, "卖家登录应该成功");
        assertTrue(UserService.isLoggedIn(), "卖家应该已登录");
        
        Long sellerId = UserService.getCurrentUser().getId();
        assertNotNull(sellerId, "卖家ID不应为空");
        
        // 2.2 卖家发布商品
        String publishResult = itemService.publishItem(
            sellerId,
            "iPhone 15 Pro",
            "全新iPhone 15 Pro，256GB，深空黑色",
            8999.0,
            "Electronics",
            "iphone.jpg"
        );
        assertNull(publishResult, "商品发布应该成功");
        
        // 2.3 验证商品已发布
        List<Item> sellerItems = itemService.getItemsBySeller(sellerId);
        assertEquals(1, sellerItems.size(), "卖家应该有1个商品");
        Item publishedItem = sellerItems.get(0);
        assertTrue(publishedItem.isActive(), "商品应该处于可售状态");
        
        userService.logout();
        
        // ============ Phase 3: 买家浏览和收藏商品 ============
        // 3.1 买家登录
        String buyerLoginResult = userService.login("buyer_user", "buyer123");
        assertNull(buyerLoginResult, "买家登录应该成功");
        
        Long buyerId = UserService.getCurrentUser().getId();
        
        // 3.2 买家浏览所有商品
        List<Item> allItems = itemService.getAllActiveItems();
        assertEquals(1, allItems.size(), "应该有1个可售商品");
        
        // 3.3 买家搜索商品
        List<Item> searchResults = itemService.searchItems("iPhone");
        assertEquals(1, searchResults.size(), "搜索应该找到1个商品");
        
        Long itemId = searchResults.get(0).getId();
        
        // 3.4 买家收藏商品
        String favoriteResult = favoriteService.addFavorite(buyerId, itemId);
        assertNull(favoriteResult, "收藏应该成功");
        
        List<Item> favoriteItems = favoriteService.getUserFavoriteItems(buyerId);
        assertEquals(1, favoriteItems.size(), "买家应该有1个收藏");
        
        // ============ Phase 4: 买家下单购买 ============
        // 4.1 买家创建订单
        String orderResult = orderService.createOrder(
            buyerId, 
            itemId, 
            "香港九龙塘达之路83号"
        );
        assertNull(orderResult, "订单创建应该成功");
        
        // 4.2 验证订单已创建
        List<Order> buyerOrders = orderService.getOrdersByBuyer(buyerId);
        assertEquals(1, buyerOrders.size(), "买家应该有1个订单");
        
        Order order = buyerOrders.get(0);
        assertEquals("PAID", order.getStatus(), "订单状态应该是已支付");
        assertEquals(8999.0, order.getAmount(), "订单金额应该正确");
        
        // 4.3 验证商品已下架
        Item soldItem = itemService.getItemById(itemId);
        assertFalse(soldItem.isActive(), "商品应该已下架");
        
        userService.logout();
        
        // ============ Phase 5: 卖家处理订单 ============
        // 5.1 卖家登录并查看订单
        userService.login("seller_user", "seller123");
        
        List<Order> sellerOrders = orderService.getOrdersBySeller(sellerId);
        assertEquals(1, sellerOrders.size(), "卖家应该有1个订单");
        
        Long orderId = sellerOrders.get(0).getId();
        
        // 5.2 卖家更新订单状态为已发货
        String shipResult = orderService.updateOrderStatus(
            orderId, 
            "SHIPPED", 
            sellerId
        );
        assertNull(shipResult, "订单状态更新应该成功");
        
        userService.logout();
        
        // 5.3 买家确认订单完成（根据业务规则，只有买家能完成订单）
        userService.login("buyer_user", "buyer123");
        
        String completeResult = orderService.updateOrderStatus(
            orderId, 
            "COMPLETED", 
            buyerId
        );
        assertNull(completeResult, "订单完成应该成功");
        
        // ============ Phase 6: 买家评价 ============
        // 6.1 买家提交评价（买家已经登录，不需要再次登录）
        String reviewResult = reviewService.addReview(
            orderId,
            buyerId,
            sellerId,
            itemId,
            5,
            "商品质量很好，卖家服务态度也很好！"
        );
        assertNull(reviewResult, "评价提交应该成功");
        
        // 6.2 验证评价已创建
        List<Review> reviews = reviewService.getSellerReviews(sellerId);
        assertEquals(1, reviews.size(), "卖家应该有1条评价");
        assertEquals(5, reviews.get(0).getRating(), "评分应该是5星");
        
        userService.logout();
        
        // ============ Phase 7: 验证通知 ============
        // 7.1 卖家查看通知
        userService.login("seller_user", "seller123");
        
        var sellerNotifications = notificationService.getUserNotifications(sellerId);
        assertTrue(sellerNotifications.size() > 0, "卖家应该有通知");
        
        userService.logout();
    }

    @Test
    @DisplayName("场景2: 多用户并发购物场景")
    void testMultiUserPurchaseScenario() {
        // 1. 创建一个卖家和两个买家
        userService.register("seller1", "pass123", "seller1@test.com", UserRole.SELLER);
        userService.register("buyer1", "pass123", "buyer1@test.com", UserRole.BUYER);
        userService.register("buyer2", "pass123", "buyer2@test.com", UserRole.BUYER);
        
        // 2. 卖家发布两个商品
        userService.login("seller1", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        
        itemService.publishItem(sellerId, "MacBook Pro", "2023款", 15999.0, "Electronics", null);
        itemService.publishItem(sellerId, "iPad Air", "2024款", 4999.0, "Electronics", null);
        userService.logout();
        
        // 3. 买家1购买MacBook
        userService.login("buyer1", "pass123");
        Long buyer1Id = UserService.getCurrentUser().getId();
        List<Item> items = itemService.searchItems("MacBook");
        assertEquals(1, items.size());
        
        orderService.createOrder(buyer1Id, items.get(0).getId(), "地址1");
        userService.logout();
        
        // 4. 买家2购买iPad
        userService.login("buyer2", "pass123");
        Long buyer2Id = UserService.getCurrentUser().getId();
        List<Item> iPadItems = itemService.searchItems("iPad");
        assertEquals(1, iPadItems.size());
        
        orderService.createOrder(buyer2Id, iPadItems.get(0).getId(), "地址2");
        userService.logout();
        
        // 5. 验证卖家有两个订单
        userService.login("seller1", "pass123");
        List<Order> orders = orderService.getOrdersBySeller(sellerId);
        assertEquals(2, orders.size(), "卖家应该有2个订单");
        userService.logout();
    }

    @Test
    @DisplayName("场景3: 异常情况处理 - 购买已下架商品")
    void testPurchaseInactiveItem() {
        // 1. 创建卖家和买家
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        userService.register("buyer", "pass123", "buyer@test.com", UserRole.BUYER);
        
        // 2. 卖家发布商品
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "Item1", "Description", 100.0, "Category", null);
        List<Item> items = itemService.getItemsBySeller(sellerId);
        Long itemId = items.get(0).getId();
        
        // 3. 卖家下架商品
        itemService.deactivateItem(itemId);
        userService.logout();
        
        // 4. 买家尝试购买已下架商品
        userService.login("buyer", "pass123");
        Long buyerId = UserService.getCurrentUser().getId();
        
        String result = orderService.createOrder(buyerId, itemId, "地址");
        assertEquals("Item is no longer available", result, "应该提示商品不可用");
        userService.logout();
    }

    @Test
    @DisplayName("场景4: 卖家不能购买自己的商品")
    void testSellerCannotBuyOwnItem() {
        // 1. 创建卖家
        userService.register("seller", "pass123", "seller@test.com", UserRole.SELLER);
        
        // 2. 卖家发布商品
        userService.login("seller", "pass123");
        Long sellerId = UserService.getCurrentUser().getId();
        itemService.publishItem(sellerId, "MyItem", "Description", 100.0, "Category", null);
        
        List<Item> items = itemService.getItemsBySeller(sellerId);
        Long itemId = items.get(0).getId();
        
        // 3. 卖家尝试购买自己的商品
        String result = orderService.createOrder(sellerId, itemId, "地址");
        assertEquals("Cannot buy your own item", result, "应该提示不能购买自己的商品");
        
        userService.logout();
    }
}

