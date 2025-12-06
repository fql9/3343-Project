package integration.dao.impl;

import integration.IntegrationTestBase;
import dao.impl.ReviewDaoImpl;
import dao.impl.UserDaoImpl;
import dao.impl.ItemDaoImpl;
import dao.impl.OrderDaoImpl;
import dao.ReviewDao;
import dao.UserDao;
import dao.ItemDao;
import dao.OrderDao;
import config.DatabaseConfig;
import model.Review;
import model.User;
import model.Item;
import model.Order;
import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 针对 ReviewDaoImpl 分支覆盖率的增强测试
 * 目标: 将分支覆盖率从 64% 提升至 90%+
 */
class ReviewDaoImplBranchCoverageTest extends IntegrationTestBase {

    private ReviewDao reviewDao;
    private UserDao userDao;
    private ItemDao itemDao;
    private OrderDao orderDao;

    @BeforeEach
    void setUp() {
        reviewDao = new ReviewDaoImpl();
        userDao = new UserDaoImpl();
        itemDao = new ItemDaoImpl();
        orderDao = new OrderDaoImpl();
    }

    // ========== findBySellerId 分支测试 ==========
    
    @Test
    void testFindBySellerId_NoReviews() {
        List<Review> reviews = reviewDao.findBySellerId(999999L);
        assertNotNull(reviews);
        assertTrue(reviews.isEmpty());
    }

    @Test
    void testFindBySellerId_WithMultipleReviews() {
        // 创建卖家和买家
        User seller = createAndSaveUser("review_seller");
        User buyer = createAndSaveUser("review_buyer");
        
        // 创建商品
        Item item = createAndSaveItem(seller.getId(), "Review Test Item");
        
        // 创建订单
        Order order1 = createAndSaveOrder(buyer.getId(), seller.getId(), item.getId());
        Order order2 = createAndSaveOrder(buyer.getId(), seller.getId(), item.getId());
        
        // 创建多个评论
        Review review1 = createReview(order1.getId(), buyer.getId(), seller.getId(), item.getId(), 5);
        Review review2 = createReview(order2.getId(), buyer.getId(), seller.getId(), item.getId(), 4);
        
        reviewDao.save(review1);
        reviewDao.save(review2);
        
        // 查询卖家的评论
        List<Review> reviews = reviewDao.findBySellerId(seller.getId());
        
        assertNotNull(reviews);
        assertEquals(2, reviews.size());
    }

    // ========== findByOrderId 分支测试 ==========
    
    @Test
    void testFindByOrderId_Exists() {
        User seller = createAndSaveUser("order_review_seller");
        User buyer = createAndSaveUser("order_review_buyer");
        Item item = createAndSaveItem(seller.getId(), "Order Review Item");
        Order order = createAndSaveOrder(buyer.getId(), seller.getId(), item.getId());
        
        Review review = createReview(order.getId(), buyer.getId(), seller.getId(), item.getId(), 5);
        reviewDao.save(review);
        
        Review found = reviewDao.findByOrderId(order.getId());
        
        assertNotNull(found);
        assertEquals(order.getId(), found.getOrderId());
        assertEquals(5, found.getRating());
    }

    @Test
    void testFindByOrderId_NotExists() {
        Review found = reviewDao.findByOrderId(999999L);
        assertNull(found);
    }

    // ========== getAverageRating 分支测试 ==========
    
    @Test
    void testGetAverageRating_NoReviews() {
        double avg = reviewDao.getAverageRating(999999L);
        assertEquals(0.0, avg);
    }

    @Test
    void testGetAverageRating_WithReviews() {
        User seller = createAndSaveUser("avg_rating_seller");
        User buyer1 = createAndSaveUser("avg_rating_buyer1");
        User buyer2 = createAndSaveUser("avg_rating_buyer2");
        
        Item item = createAndSaveItem(seller.getId(), "Avg Rating Item");
        
        Order order1 = createAndSaveOrder(buyer1.getId(), seller.getId(), item.getId());
        Order order2 = createAndSaveOrder(buyer2.getId(), seller.getId(), item.getId());
        
        // 一个 5 星，一个 3 星，平均应该是 4
        Review review1 = createReview(order1.getId(), buyer1.getId(), seller.getId(), item.getId(), 5);
        Review review2 = createReview(order2.getId(), buyer2.getId(), seller.getId(), item.getId(), 3);
        
        reviewDao.save(review1);
        reviewDao.save(review2);
        
        double avg = reviewDao.getAverageRating(seller.getId());
        
        assertEquals(4.0, avg, 0.01);
    }

    @Test
    void testGetAverageRating_SingleReview() {
        User seller = createAndSaveUser("single_rating_seller");
        User buyer = createAndSaveUser("single_rating_buyer");
        
        Item item = createAndSaveItem(seller.getId(), "Single Rating Item");
        Order order = createAndSaveOrder(buyer.getId(), seller.getId(), item.getId());
        
        Review review = createReview(order.getId(), buyer.getId(), seller.getId(), item.getId(), 4);
        reviewDao.save(review);
        
        double avg = reviewDao.getAverageRating(seller.getId());
        
        assertEquals(4.0, avg, 0.01);
    }

    // ========== save 分支测试 ==========
    
    @Test
    void testSave_WithComment() {
        User seller = createAndSaveUser("save_review_seller");
        User buyer = createAndSaveUser("save_review_buyer");
        Item item = createAndSaveItem(seller.getId(), "Save Review Item");
        Order order = createAndSaveOrder(buyer.getId(), seller.getId(), item.getId());
        
        Review review = createReview(order.getId(), buyer.getId(), seller.getId(), item.getId(), 5);
        review.setComment("Excellent product and fast shipping!");
        
        reviewDao.save(review);
        
        Review found = reviewDao.findByOrderId(order.getId());
        assertNotNull(found);
        assertEquals("Excellent product and fast shipping!", found.getComment());
    }

    @Test
    void testSave_WithoutComment() {
        User seller = createAndSaveUser("nocomment_seller");
        User buyer = createAndSaveUser("nocomment_buyer");
        Item item = createAndSaveItem(seller.getId(), "No Comment Item");
        Order order = createAndSaveOrder(buyer.getId(), seller.getId(), item.getId());
        
        Review review = createReview(order.getId(), buyer.getId(), seller.getId(), item.getId(), 3);
        review.setComment(null);
        
        reviewDao.save(review);
        
        Review found = reviewDao.findByOrderId(order.getId());
        assertNotNull(found);
        assertNull(found.getComment());
    }

    // ========== 辅助方法 ==========
    
    private User createAndSaveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash123");
        user.setEmail(username + "@test.com");
        user.setRole(UserRole.SELLER);
        user.setActive(true);
        user.setCreatedTime("2024-01-01 00:00:00");
        userDao.save(user);
        return userDao.findByUsername(username);
    }
    
    private Item createAndSaveItem(Long sellerId, String title) {
        Item item = new Item();
        item.setSellerId(sellerId);
        item.setTitle(title);
        item.setDescription("Test description");
        item.setPrice(100.0);
        item.setCategory("Electronics");
        item.setActive(true);
        item.setCreatedTime("2024-01-01 00:00:00");
        itemDao.save(item);
        
        // 获取保存后的 Item（带 ID）
        List<Item> items = itemDao.findBySellerId(sellerId);
        return items.get(items.size() - 1);
    }
    
    private Order createAndSaveOrder(Long buyerId, Long sellerId, Long itemId) {
        Order order = new Order();
        order.setOrderNo("ORD" + UUID.randomUUID().toString().substring(0, 8));
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setItemId(itemId);
        order.setAmount(100.0);
        order.setStatus("COMPLETED");
        order.setShippingAddress("123 Test St");
        order.setCreatedTime("2024-01-01 00:00:00");
        orderDao.save(order);
        
        // 获取保存后的 Order
        List<Order> orders = orderDao.findByBuyerId(buyerId);
        return orders.get(orders.size() - 1);
    }
    
    private Review createReview(Long orderId, Long reviewerId, Long revieweeId, Long itemId, int rating) {
        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setItemId(itemId);
        review.setRating(rating);
        review.setCreatedTime("2024-01-01 00:00:00");
        return review;
    }
}

