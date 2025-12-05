package service;

import dao.ReviewDao;
import dao.impl.ReviewDaoImpl;
import model.Review;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReviewService {

    private final ReviewDao reviewDao;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReviewService() {
        this.reviewDao = new ReviewDaoImpl();
    }

    public String addReview(Long orderId, Long reviewerId, Long revieweeId, Long itemId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            return "Rating must be between 1 and 5";
        }
        
        if (reviewDao.findByOrderId(orderId) != null) {
            return "You have already reviewed this order";
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setItemId(itemId);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedTime(LocalDateTime.now().format(DATE_FORMATTER));

        reviewDao.save(review);
        return null;
    }

    public List<Review> getSellerReviews(Long sellerId) {
        return reviewDao.findBySellerId(sellerId);
    }

    public double getSellerAverageRating(Long sellerId) {
        return reviewDao.getAverageRating(sellerId);
    }
    
    public boolean hasReviewed(Long orderId) {
        return reviewDao.findByOrderId(orderId) != null;
    }
}
