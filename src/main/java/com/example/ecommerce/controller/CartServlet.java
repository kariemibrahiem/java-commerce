package com.example.ecommerce.controller;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.service.CartService;
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

@WebServlet("/api/cart/*")
public class CartServlet extends HttpServlet {

    private CartService cartService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        cartService = new CartService();
        gson = new Gson();
    }

    // View Cart
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", null);
            return;
        }

        try {
            List<CartItem> cartItems = cartService.getCart(userId);
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Cart retrieved successfully", cartItems);
        } catch (Exception e) {
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve cart items.", null);
        }
    }

    // Add to Cart
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", null);
            return;
        }

        try (BufferedReader reader = req.getReader()) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            
            if (!jsonObject.has("productId")) {
                com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "productId is required.", null);
                return;
            }
            
            int productId = jsonObject.get("productId").getAsInt();
            int quantity = jsonObject.has("quantity") ? jsonObject.get("quantity").getAsInt() : 1;

            boolean success = cartService.addToCart(userId, productId, quantity);
            if (success) {
                com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_CREATED, "Product added to cart successfully.", null);
            } else {
                com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Failed to add product to cart.", null);
            }
        } catch (Exception e) {
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request format.", null);
        }
    }

    // Delete from Cart (single item or all)
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", null);
            return;
        }

        String productIdParam = req.getParameter("productId");
        
        try {
            if (productIdParam != null && !productIdParam.isEmpty()) {
                // Delete specific product
                int productId = Integer.parseInt(productIdParam);
                boolean success = cartService.deleteFromCart(userId, productId);
                if (success) {
                    com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Product removed from cart successfully.", null);
                } else {
                    com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Product not found in cart.", null);
                }
            } else {
                // Clear entire cart
                boolean success = cartService.clearCart(userId);
                com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Cart cleared successfully.", null);
            }
        } catch (Exception e) {
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request format.", null);
        }
    }
}
