package integration.dao.impl;

import dao.ReviewDao;
import dao.impl.ReviewDaoImpl;
import integration.IntegrationTestBase;
import model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReviewDaoImpl 集成测试
 */
class ReviewDaoImplTest extends IntegrationTestBase {

    private ReviewDao reviewDao;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        reviewDao = new ReviewDaoImpl();
    }

    @Test
    void testSaveReview() {
        // Arrange
        Review review = createTestReview(1L, 1L, 2L, 1L, 5, "Excellent!");

        // Act
        reviewDao.save(review);

        // Assert
        Review found = reviewDao.findByOrderId(1L);
        assertNotNull(found);
        assertEquals(5, found.getRating());
        assertEquals("Excellent!", found.getComment());
    }

    @Test
    void testFindBySellerId() {
        // Arrange
        Review review1 = createTestReview(1L, 1L, 100L, 1L, 5, "Great!");
        Review review2 = createTestReview(2L, 2L, 100L, 2L, 4, "Good!");
        Review review3 = createTestReview(3L, 3L, 200L, 3L, 3, "OK");

        reviewDao.save(review1);
        reviewDao.save(review2);
        reviewDao.save(review3);

        // Act
        List<Review> reviews = reviewDao.findBySellerId(100L);

        // Assert
        assertEquals(2, reviews.size());
        assertTrue(reviews.stream().anyMatch(r -> r.getRating() == 5));
        assertTrue(reviews.stream().anyMatch(r -> r.getRating() == 4));
    }

    @Test
    void testFindByOrderId() {
        // Arrange
        Review review = createTestReview(999L, 1L, 2L, 1L, 5, "Perfect!");
        reviewDao.save(review);

        // Act
        Review found = reviewDao.findByOrderId(999L);

        // Assert
        assertNotNull(found);
        assertEquals(999L, found.getOrderId());
        assertEquals(5, found.getRating());
        assertEquals("Perfect!", found.getComment());
    }

    @Test
    void testFindByOrderId_NotFound() {
        // Act
        Review found = reviewDao.findByOrderId(99999L);

        // Assert
        assertNull(found);
    }

    @Test
    void testGetAverageRating() {
        // Arrange
        Long sellerId = 500L;
        reviewDao.save(createTestReview(1L, 1L, sellerId, 1L, 5, "Great"));
        reviewDao.save(createTestReview(2L, 2L, sellerId, 2L, 4, "Good"));
        reviewDao.save(createTestReview(3L, 3L, sellerId, 3L, 3, "OK"));

        // Act
        double average = reviewDao.getAverageRating(sellerId);

        // Assert
        assertEquals(4.0, average, 0.01); // (5+4+3)/3 = 4.0
    }

    @Test
    void testGetAverageRating_NoReviews() {
        // Act
        double average = reviewDao.getAverageRating(99999L);

        // Assert
        assertEquals(0.0, average, 0.01);
    }

    @Test
    void testMultipleReviewsForSameSeller() {
        // Arrange
        Long sellerId = 300L;
        for (int i = 1; i <= 5; i++) {
            reviewDao.save(createTestReview(
                (long) i, 
                (long) i, 
                sellerId, 
                (long) i, 
                i, 
                "Review " + i
            ));
        }

        // Act
        List<Review> reviews = reviewDao.findBySellerId(sellerId);

        // Assert
        assertEquals(5, reviews.size());
        
        // 验证按时间倒序排列（最新的在前）
        for (int i = 0; i < reviews.size() - 1; i++) {
            assertTrue(reviews.get(i).getCreatedTime()
                .compareTo(reviews.get(i + 1).getCreatedTime()) >= 0);
        }
    }

    @Test
    void testReviewWithDifferentRatings() {
        // 测试所有评分等级
        Long sellerId = 400L;
        for (int rating = 1; rating <= 5; rating++) {
            reviewDao.save(createTestReview(
                (long) rating, 
                (long) rating, 
                sellerId, 
                (long) rating, 
                rating, 
                "Rating: " + rating
            ));
        }

        // Act
        List<Review> reviews = reviewDao.findBySellerId(sellerId);
        double average = reviewDao.getAverageRating(sellerId);

        // Assert
        assertEquals(5, reviews.size());
        assertEquals(3.0, average, 0.01); // (1+2+3+4+5)/5 = 3.0
    }

    @Test
    void testReviewWithLongComment() {
        // Arrange
        String longComment = "A".repeat(500); // 500字符的评论
        Review review = createTestReview(1L, 1L, 2L, 1L, 5, longComment);

        // Act
        reviewDao.save(review);
        Review found = reviewDao.findByOrderId(1L);

        // Assert
        assertNotNull(found);
        assertEquals(longComment, found.getComment());
    }

    @Test
    void testReviewWithEmptyComment() {
        // Arrange
        Review review = createTestReview(1L, 1L, 2L, 1L, 3, "");

        // Act
        reviewDao.save(review);
        Review found = reviewDao.findByOrderId(1L);

        // Assert
        assertNotNull(found);
        assertEquals("", found.getComment());
    }

    // Helper method
    private Review createTestReview(Long orderId, Long reviewerId, Long revieweeId, 
                                   Long itemId, int rating, String comment) {
        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setItemId(itemId);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        return review;
    }
}

