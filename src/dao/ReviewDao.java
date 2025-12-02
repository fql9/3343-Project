package dao;

import model.Review;
import java.util.List;

/**
 * Review Data Access Object interface.
 * Provides database operations for Review entity.
 */
public interface ReviewDao {
    void save(Review review);
    List<Review> findBySellerId(Long sellerId);
    Review findByOrderId(Long orderId);
    double getAverageRating(Long sellerId);
}
