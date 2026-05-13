package com.example.ecommerce.dao;

import com.example.ecommerce.config.DatabaseConnection;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {




    public Order createOrderFromCart(int userId) {
        String selectCartSql = "SELECT c.product_id, c.quantity, p.price FROM cart_items c JOIN products p ON c.product_id = p.id WHERE c.user_id = ?";
        String insertOrderSql = "INSERT INTO orders (user_id, total_price) VALUES (?, ?)";
        String insertOrderItemSql = "INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
        String clearCartSql = "DELETE FROM cart_items WHERE user_id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Get cart items and calculate total
            BigDecimal total = BigDecimal.ZERO;
            List<OrderItem> tempItems = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(selectCartSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPriceAtPurchase(rs.getBigDecimal("price"));
                    
                    BigDecimal itemTotal = item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()));
                    total = total.add(itemTotal);
                    
                    tempItems.add(item);
                }
            }

            if (tempItems.isEmpty()) {
                conn.rollback();
                return null; // Cart is empty
            }

            // 2. Insert Order
            int orderId = 0;
            try (PreparedStatement stmt = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setBigDecimal(2, total);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    orderId = rs.getInt(1);
                }
            }

            // 3. Insert Order Items
            try (PreparedStatement stmt = conn.prepareStatement(insertOrderItemSql)) {
                for (OrderItem item : tempItems) {
                    stmt.setInt(1, orderId);
                    stmt.setInt(2, item.getProductId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setBigDecimal(4, item.getPriceAtPurchase());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // 3.5 Commit reserved stock
            String commitStockSql = "UPDATE products SET reserved_stock = reserved_stock - ? WHERE id = ? AND reserved_stock >= ?";
            try (PreparedStatement stmt = conn.prepareStatement(commitStockSql)) {
                for (OrderItem item : tempItems) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setInt(2, item.getProductId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // 4. Clear Cart
            try (PreparedStatement stmt = conn.prepareStatement(clearCartSql)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            
            Order order = new Order();
            order.setId(orderId);
            order.setUserId(userId);
            order.setTotalPrice(total);
            order.setStatus("PENDING");
            return order;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        String itemsSql = "SELECT oi.*, p.name FROM order_items oi JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setTotalPrice(rs.getBigDecimal("total_price"));
                order.setStatus(rs.getString("status"));
                order.setCreatedAt(rs.getTimestamp("created_at"));
                
                List<OrderItem> items = new ArrayList<>();
                try (PreparedStatement itemStmt = conn.prepareStatement(itemsSql)) {
                    itemStmt.setInt(1, order.getId());
                    ResultSet itemRs = itemStmt.executeQuery();
                    while (itemRs.next()) {
                        OrderItem item = new OrderItem();
                        item.setId(itemRs.getInt("id"));
                        item.setOrderId(itemRs.getInt("order_id"));
                        item.setProductId(itemRs.getInt("product_id"));
                        item.setQuantity(itemRs.getInt("quantity"));
                        item.setPriceAtPurchase(itemRs.getBigDecimal("price_at_purchase"));
                        
                        Product p = new Product();
                        p.setId(item.getProductId());
                        p.setName(itemRs.getString("name"));
                        item.setProduct(p);
                        
                        items.add(item);
                    }
                }
                order.setItems(items);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
}
