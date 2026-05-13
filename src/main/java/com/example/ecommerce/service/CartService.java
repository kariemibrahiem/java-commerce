package com.example.ecommerce.service;

import com.example.ecommerce.dao.CartDAO;
import com.example.ecommerce.model.CartItem;

import com.example.ecommerce.dao.ProductDAO;

import java.util.List;

public class CartService {
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartService() {
        this.cartDAO = new CartDAO();
        this.productDAO = new ProductDAO();
    }

    public boolean addToCart(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        
        List<CartItem> items = cartDAO.getCartItemsByUserId(userId);
        CartItem existing = items.stream().filter(i -> i.getProductId() == productId).findFirst().orElse(null);
        int oldQuantity = existing != null ? existing.getQuantity() : 0;
        int diff = quantity - oldQuantity;
        
        if (diff > 0) {
            boolean reserved = productDAO.reserveStock(productId, diff);
            if (!reserved) {
                return false; // Not enough stock
            }
        } else if (diff < 0) {
            productDAO.releaseReservedStock(productId, -diff);
        }
        
        return cartDAO.addOrUpdateCartItem(userId, productId, quantity);
    }

    public List<CartItem> getCart(int userId) {
        return cartDAO.getCartItemsByUserId(userId);
    }

    public boolean deleteFromCart(int userId, int productId) {
        List<CartItem> items = cartDAO.getCartItemsByUserId(userId);
        CartItem existing = items.stream().filter(i -> i.getProductId() == productId).findFirst().orElse(null);
        if (existing != null) {
            productDAO.releaseReservedStock(productId, existing.getQuantity());
        }
        return cartDAO.deleteCartItem(userId, productId);
    }

    public boolean clearCart(int userId) {
        List<CartItem> items = cartDAO.getCartItemsByUserId(userId);
        for (CartItem item : items) {
            productDAO.releaseReservedStock(item.getProductId(), item.getQuantity());
        }
        return cartDAO.clearCart(userId);
    }
}
