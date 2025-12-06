package integration.service;

import integration.IntegrationTestBase;
import service.ReviewService;
import service.UserService;
import service.ItemService;
import service.OrderService;

import config.DatabaseConfig;
import model.Item;
import model.Order;
import model.Review;
import model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewServiceTest extends IntegrationTestBase {

    private ReviewService reviewService;
    private UserService userService;
    private ItemService itemService;
    private OrderService orderService;
    
    private Long testBuyerId;
    private Long testSellerId;
    private Long testItemId;
    private Long testOrderId;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService();
        userService = new UserService();
        itemService = new ItemService();
        orderService = new OrderService();
        
        // Clean up test data
        cleanupTestData();
        
        // Create test buyer
        userService.register("review_test_buyer", "password123", "buyer@test.com", UserRole.BUYER);
        userService.login("review_test_buyer", "password123");
        testBuyerId = UserService.getCurrentUser().getId();
        userService.logout();
        
        // Create test seller
        userService.register("review_test_seller", "password123", "seller@test.com", UserRole.SELLER);
        userService.login("review_test_seller", "password123");
        testSellerId = UserService.getCurrentUser().getId();
        
        // Create test item
        itemService.publishItem(testSellerId, "Test Item", "Test Description", 100.0, "Electronics", null);
        List<Item> items = itemService.getItemsBySeller(testSellerId);
        testItemId = items.get(0).getId();
        userService.logout();
        
        // Create test order
        userService.login("review_test_buyer", "password123");
        orderService.createOrder(testBuyerId, testItemId, "123 Test St");
        // Get the created order
        List<Order> orders = orderService.getOrdersByBuyer(testBuyerId);
        testOrderId = orders.get(0).getId();
        userService.logout();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try (java.sql.Connection conn = DatabaseConfig.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            // Delete test reviews
            stmt.executeUpdate("DELETE FROM reviews WHERE reviewer_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'review_test_%')");
            stmt.executeUpdate("DELETE FROM reviews WHERE reviewee_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'review_test_%')");
            // Delete test orders
            stmt.executeUpdate("DELETE FROM orders WHERE buyer_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'review_test_%')");
            stmt.executeUpdate("DELETE FROM orders WHERE seller_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'review_test_%')");
            // Delete test items
            stmt.executeUpdate("DELETE FROM items WHERE seller_id IN " +
                    "(SELECT id FROM users WHERE username LIKE 'review_test_%')");
            // Delete test users
            stmt.executeUpdate("DELETE FROM users WHERE username LIKE 'review_test_%'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAddReview_Success() {
        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 5, "Excellent seller!");

        // Assert
        assertNull(result, "Adding review should succeed");
        
        // Verify review was saved
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertFalse(reviews.isEmpty(), "Seller should have reviews");
        assertEquals(5, reviews.get(0).getRating());
        assertEquals("Excellent seller!", reviews.get(0).getComment());
        assertEquals(testBuyerId, reviews.get(0).getReviewerId());
        assertEquals(testSellerId, reviews.get(0).getRevieweeId());
        assertEquals(testItemId, reviews.get(0).getItemId());
        assertEquals(testOrderId, reviews.get(0).getOrderId());
    }

    @Test
    void testAddReview_RatingTooLow() {
        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 0, "Bad rating");

        // Assert
        assertEquals("Rating must be between 1 and 5", result);
        
        // Verify review was not saved
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertTrue(reviews.isEmpty());
    }

    @Test
    void testAddReview_RatingTooHigh() {
        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 6, "Rating too high");

        // Assert
        assertEquals("Rating must be between 1 and 5", result);
        
        // Verify review was not saved
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertTrue(reviews.isEmpty());
    }

    @Test
    void testAddReview_NegativeRating() {
        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, -1, "Negative rating");

        // Assert
        assertEquals("Rating must be between 1 and 5", result);
    }

    @Test
    void testAddReview_MinimumValidRating() {
        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 1, "Minimum rating");

        // Assert
        assertNull(result, "Rating 1 should be valid");
        
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertEquals(1, reviews.get(0).getRating());
    }

    @Test
    void testAddReview_MaximumValidRating() {
        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 5, "Maximum rating");

        // Assert
        assertNull(result, "Rating 5 should be valid");
        
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertEquals(5, reviews.get(0).getRating());
    }

    @Test
    void testAddReview_DuplicateReview() {
        // Arrange - Add first review
        reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 5, "First review");

        // Act - Try to add second review for same order
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 4, "Second review");

        // Assert
        assertEquals("You have already reviewed this order", result);
        
        // Verify only one review exists
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertEquals(1, reviews.size());
        assertEquals("First review", reviews.get(0).getComment());
    }

    @Test
    void testAddReview_WithEmptyComment() {
        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 4, "");

        // Assert
        assertNull(result, "Empty comment should be allowed");
        
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertEquals("", reviews.get(0).getComment());
    }

    @Test
    void testAddReview_WithNullComment() {
        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 4, null);

        // Assert
        assertNull(result, "Null comment should be allowed");
        
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertNull(reviews.get(0).getComment());
    }

    @Test
    void testAddReview_WithLongComment() {
        // Arrange
        String longComment = "This is a very long comment. ".repeat(20);

        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 5, longComment);

        // Assert
        assertNull(result);
        
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertEquals(longComment, reviews.get(0).getComment());
    }

    @Test
    void testAddReview_WithSpecialCharacters() {
        // Arrange
        String specialComment = "Great! @#$%^&*()_+-=[]{}|;':\",./<>?`~";

        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 5, specialComment);

        // Assert
        assertNull(result);
        
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertEquals(specialComment, reviews.get(0).getComment());
    }

    @Test
    void testAddReview_WithUnicode() {
        // Arrange
        String unicodeComment = "很好 excellent すばらしい 👍🌟";

        // Act
        String result = reviewService.addReview(testOrderId, testBuyerId, testSellerId, 
                testItemId, 5, unicodeComment);

        // Assert
        assertNull(result);
        
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertEquals(unicodeComment, reviews.get(0).getComment());
    }

    @Test
    void testGetSellerReviews_NoReviews() {
        // Act
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);

        // Assert
        assertNotNull(reviews);
        assertTrue(reviews.isEmpty());
    }

    @Test
    void testGetSellerReviews_MultipleReviews() {
        // Arrange - Create multiple buyers and orders
        userService.register("review_test_buyer2", "password123", "buyer2@test.com", UserRole.BUYER);
        userService.login("review_test_buyer2", "password123");
        Long buyer2Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("review_test_seller", "password123");
        itemService.publishItem(testSellerId, "Test Item 2", "Description", 50.0, "Electronics", null);
        List<Item> sellerItems = itemService.getItemsBySeller(testSellerId);
        Long item2Id = sellerItems.get(sellerItems.size() - 1).getId();
        userService.logout();
        
        userService.login("review_test_buyer2", "password123");
        orderService.createOrder(buyer2Id, item2Id, "456 Test Ave");
        List<Order> buyer2Orders = orderService.getOrdersByBuyer(buyer2Id);
        Long order2Id = buyer2Orders.get(0).getId();
        userService.logout();
        
        // Add reviews
        reviewService.addReview(testOrderId, testBuyerId, testSellerId, testItemId, 5, "Great!");
        reviewService.addReview(order2Id, buyer2Id, testSellerId, item2Id, 4, "Good!");

        // Act
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);

        // Assert
        assertNotNull(reviews);
        assertEquals(2, reviews.size());
    }

    @Test
    void testGetSellerAverageRating_NoReviews() {
        // Act
        double avgRating = reviewService.getSellerAverageRating(testSellerId);

        // Assert
        assertEquals(0.0, avgRating, 0.01);
    }

    @Test
    void testGetSellerAverageRating_SingleReview() {
        // Arrange
        reviewService.addReview(testOrderId, testBuyerId, testSellerId, testItemId, 4, "Good");

        // Act
        double avgRating = reviewService.getSellerAverageRating(testSellerId);

        // Assert
        assertEquals(4.0, avgRating, 0.01);
    }

    @Test
    void testGetSellerAverageRating_MultipleReviews() {
        // Arrange - Create second buyer and order
        userService.register("review_test_buyer2", "password123", "buyer2@test.com", UserRole.BUYER);
        userService.login("review_test_buyer2", "password123");
        Long buyer2Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("review_test_seller", "password123");
        itemService.publishItem(testSellerId, "Test Item 2", "Description", 50.0, "Electronics", null);
        List<Item> items2 = itemService.getItemsBySeller(testSellerId);
        Long item2Id = items2.get(items2.size() - 1).getId();
        userService.logout();
        
        userService.login("review_test_buyer2", "password123");
        orderService.createOrder(buyer2Id, item2Id, "456 Test Ave");
        List<Order> orders2 = orderService.getOrdersByBuyer(buyer2Id);
        Long order2Id = orders2.get(0).getId();
        userService.logout();
        
        // Add reviews with ratings 5, 3 (average = 4.0)
        reviewService.addReview(testOrderId, testBuyerId, testSellerId, testItemId, 5, "Excellent");
        reviewService.addReview(order2Id, buyer2Id, testSellerId, item2Id, 3, "Okay");

        // Act
        double avgRating = reviewService.getSellerAverageRating(testSellerId);

        // Assert
        assertEquals(4.0, avgRating, 0.01);
    }

    @Test
    void testGetSellerAverageRating_AllFiveStars() {
        // Arrange - Create second buyer and order
        userService.register("review_test_buyer2", "password123", "buyer2@test.com", UserRole.BUYER);
        userService.login("review_test_buyer2", "password123");
        Long buyer2Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("review_test_seller", "password123");
        itemService.publishItem(testSellerId, "Test Item 2", "Description", 50.0, "Electronics", null);
        List<Item> items2 = itemService.getItemsBySeller(testSellerId);
        Long item2Id = items2.get(items2.size() - 1).getId();
        userService.logout();
        
        userService.login("review_test_buyer2", "password123");
        orderService.createOrder(buyer2Id, item2Id, "456 Test Ave");
        List<Order> orders2 = orderService.getOrdersByBuyer(buyer2Id);
        Long order2Id = orders2.get(0).getId();
        userService.logout();
        
        reviewService.addReview(testOrderId, testBuyerId, testSellerId, testItemId, 5, "Perfect");
        reviewService.addReview(order2Id, buyer2Id, testSellerId, item2Id, 5, "Amazing");

        // Act
        double avgRating = reviewService.getSellerAverageRating(testSellerId);

        // Assert
        assertEquals(5.0, avgRating, 0.01);
    }

    @Test
    void testGetSellerAverageRating_AllOneStar() {
        // Arrange
        userService.register("review_test_buyer2", "password123", "buyer2@test.com", UserRole.BUYER);
        userService.login("review_test_buyer2", "password123");
        Long buyer2Id = UserService.getCurrentUser().getId();
        userService.logout();
        
        userService.login("review_test_seller", "password123");
        itemService.publishItem(testSellerId, "Test Item 2", "Description", 50.0, "Electronics", null);
        List<Item> items2 = itemService.getItemsBySeller(testSellerId);
        Long item2Id = items2.get(items2.size() - 1).getId();
        userService.logout();
        
        userService.login("review_test_buyer2", "password123");
        orderService.createOrder(buyer2Id, item2Id, "456 Test Ave");
        List<Order> orders2 = orderService.getOrdersByBuyer(buyer2Id);
        Long order2Id = orders2.get(0).getId();
        userService.logout();
        
        reviewService.addReview(testOrderId, testBuyerId, testSellerId, testItemId, 1, "Terrible");
        reviewService.addReview(order2Id, buyer2Id, testSellerId, item2Id, 1, "Bad");

        // Act
        double avgRating = reviewService.getSellerAverageRating(testSellerId);

        // Assert
        assertEquals(1.0, avgRating, 0.01);
    }

    @Test
    void testHasReviewed_NotReviewed() {
        // Act
        boolean hasReviewed = reviewService.hasReviewed(testOrderId);

        // Assert
        assertFalse(hasReviewed);
    }

    @Test
    void testHasReviewed_AlreadyReviewed() {
        // Arrange
        reviewService.addReview(testOrderId, testBuyerId, testSellerId, testItemId, 5, "Great!");

        // Act
        boolean hasReviewed = reviewService.hasReviewed(testOrderId);

        // Assert
        assertTrue(hasReviewed);
    }

    @Test
    void testHasReviewed_NonExistentOrder() {
        // Act
        boolean hasReviewed = reviewService.hasReviewed(99999L);

        // Assert
        assertFalse(hasReviewed);
    }

    @Test
    void testReviewTimestamp() {
        // Arrange & Act
        reviewService.addReview(testOrderId, testBuyerId, testSellerId, testItemId, 5, "Test");
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);

        // Assert
        assertNotNull(reviews.get(0).getCreatedTime());
        assertFalse(reviews.get(0).getCreatedTime().isEmpty());
        // Verify timestamp format (yyyy-MM-dd HH:mm:ss)
        assertTrue(reviews.get(0).getCreatedTime().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testAddReview_AllRatingValues() {
        // Test each valid rating value
        int reviewsAdded = 0;
        for (int rating = 1; rating <= 5; rating++) {
            // Create new item and order for each rating
            userService.login("review_test_seller", "password123");
            itemService.publishItem(testSellerId, "Test Item " + rating, "Description", 100.0, "Electronics", null);
                List<Item> sellerItems = itemService.getItemsBySeller(testSellerId);
                // Pick the newest item (max id) to avoid reusing inactive items from previous iterations
                Long itemId = sellerItems.stream()
                    .mapToLong(Item::getId)
                    .max()
                    .orElseThrow();
            userService.logout();
            
            userService.login("review_test_buyer", "password123");
                String orderResult = orderService.createOrder(testBuyerId, itemId, "123 Test St");
                assertNull(orderResult, "Order creation should succeed for rating " + rating);

                List<Order> buyerOrders = orderService.getOrdersByBuyer(testBuyerId);
                // Select the order created for the current item to avoid reusing previous orders
                Long orderId = buyerOrders.stream()
                    .filter(o -> o.getItemId().equals(itemId))
                    .findFirst()
                    .orElseThrow()
                    .getId();
            userService.logout();
            
            // Act
            String result = reviewService.addReview(orderId, testBuyerId, testSellerId, 
                    itemId, rating, "Rating " + rating);

            // Assert
            assertNull(result, "Rating " + rating + " should be valid");
            reviewsAdded++;
        }
        
        // Verify all reviews were added
        List<Review> reviews = reviewService.getSellerReviews(testSellerId);
        assertEquals(reviewsAdded, reviews.size());
    }

    @Test
    void testGetSellerReviews_DifferentSellers() {
        // Arrange - Create another seller
        userService.register("review_test_seller2", "password123", "seller2@test.com", UserRole.SELLER);
        userService.login("review_test_seller2", "password123");
        Long seller2Id = UserService.getCurrentUser().getId();
        itemService.publishItem(seller2Id, "Test Item 2", "Description", 50.0, "Electronics", null);
        List<Item> seller2Items = itemService.getItemsBySeller(seller2Id);
        Long item2Id = seller2Items.get(0).getId();
        userService.logout();
        
        userService.login("review_test_buyer", "password123");
        orderService.createOrder(testBuyerId, item2Id, "789 Test Blvd");
        List<Order> buyerOrders = orderService.getOrdersByBuyer(testBuyerId);
        Long order2Id = buyerOrders.get(buyerOrders.size() - 1).getId();
        userService.logout();
        
        // Add reviews for different sellers
        reviewService.addReview(testOrderId, testBuyerId, testSellerId, testItemId, 5, "Seller 1");
        reviewService.addReview(order2Id, testBuyerId, seller2Id, item2Id, 3, "Seller 2");

        // Act
        List<Review> seller1Reviews = reviewService.getSellerReviews(testSellerId);
        List<Review> seller2Reviews = reviewService.getSellerReviews(seller2Id);

        // Assert
        assertEquals(1, seller1Reviews.size());
        assertEquals(1, seller2Reviews.size());
        assertEquals("Seller 1", seller1Reviews.get(0).getComment());
        assertEquals("Seller 2", seller2Reviews.get(0).getComment());
    }
}
