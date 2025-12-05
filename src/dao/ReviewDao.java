package dao;

import model.Review;
import java.util.List;

public interface ReviewDao {
    void save(Review review);
    List<Review> findBySellerId(Long sellerId);
    Review findByOrderId(Long orderId);
    double getAverageRating(Long sellerId);
}
