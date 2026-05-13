package com.example.ecommerce.controller;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.service.OrderService;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/order/*")
public class OrderServlet extends HttpServlet {

    private OrderService orderService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", null);
            return;
        }

        try {
            List<Order> orders = orderService.getUserOrders(userId);
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Orders retrieved successfully", orders);
        } catch (Exception e) {
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve orders.", null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", null);
            return;
        }

        try {
            Order order = orderService.checkoutCart(userId);
            if (order != null) {
                com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_CREATED, "Order created successfully", order);
            } else {
                com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Cart is empty or checkout failed.", null);
            }
        } catch (Exception e) {
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create order.", null);
        }
    }
}
