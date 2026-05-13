package com.example.ecommerce.service;

import com.example.ecommerce.dao.OrderDAO;
import com.example.ecommerce.model.Order;

import java.util.List;

public class OrderService {
    private final OrderDAO orderDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
    }

    public Order checkoutCart(int userId) {
        return orderDAO.createOrderFromCart(userId);
    }

    public List<Order> getUserOrders(int userId) {
        return orderDAO.getOrdersByUserId(userId);
    }
}
