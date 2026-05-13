package com.example.ecommerce.controller;

import com.example.ecommerce.model.Review;
import com.example.ecommerce.service.ReviewService;
import com.example.ecommerce.util.ResponseUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/reviews/*")
public class ReviewServlet extends HttpServlet {

    private ReviewService reviewService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        reviewService = new ReviewService();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String productIdParam = req.getParameter("productId");
            if (productIdParam != null && !productIdParam.isEmpty()) {
                int productId = Integer.parseInt(productIdParam);
                List<Review> reviews = reviewService.getReviewsByProductId(productId);
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Reviews retrieved successfully", reviews);
            } else {
                List<Review> reviews = reviewService.getAllReviews();
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "All reviews retrieved successfully", reviews);
            }
        } catch (NumberFormatException e) {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID", null);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            JsonObject jsonBody = gson.fromJson(buffer.toString(), JsonObject.class);
            if (!jsonBody.has("productId") || !jsonBody.has("rating") || !jsonBody.has("comment")) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing productId, rating, or comment", null);
                return;
            }

            // Retrieve userId from JWT attributes set by JwtAuthFilter
            Integer userId = (Integer) req.getAttribute("userId");
            if (userId == null) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "User ID missing from token", null);
                return;
            }

            int productId = jsonBody.get("productId").getAsInt();
            int rating = jsonBody.get("rating").getAsInt();
            String comment = jsonBody.get("comment").getAsString();

            Review review = new Review(0, userId, productId, rating, comment, null);
            boolean success = reviewService.addReview(review);

            if (success) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_CREATED, "Review added successfully", review);
            } else {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add review", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                boolean success = reviewService.deleteReview(id);
                if (success) {
                    ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Review deleted successfully", null);
                } else {
                    ResponseUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Review not found or delete failed", null);
                }
            } catch (NumberFormatException e) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid review ID", null);
            } catch (Exception e) {
                e.printStackTrace();
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
            }
        } else {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing review ID", null);
        }
    }
}
