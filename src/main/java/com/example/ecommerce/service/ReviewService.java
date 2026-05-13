package com.example.ecommerce.service;

import com.example.ecommerce.dao.ReviewDAO;
import com.example.ecommerce.model.Review;

import java.util.List;

public class ReviewService {
    private final ReviewDAO reviewDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
    }

    public boolean addReview(Review review) {
        return reviewDAO.save(review);
    }

    public List<Review> getReviewsByProductId(int productId) {
        return reviewDAO.findByProductId(productId);
    }
    
    public List<Review> getAllReviews() {
        return reviewDAO.findAll();
    }

    public boolean deleteReview(int id) {
        return reviewDAO.delete(id);
    }
}
