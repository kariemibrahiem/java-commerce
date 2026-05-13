package com.example.ecommerce.controller;

import com.example.ecommerce.config.DatabaseConnection;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/stats")
public class StatsServlet extends HttpServlet {

    private Gson gson;

    @Override
    public void init() throws ServletException {
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Integer> stats = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            stats.put("users", getCount(conn, "users"));
            stats.put("products", getCount(conn, "products"));
            stats.put("orders", getCount(conn, "orders"));
            stats.put("reviews", getCount(conn, "reviews"));
            
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Stats loaded successfully", stats);
        } catch (Exception e) {
            e.printStackTrace();
            com.example.ecommerce.util.ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load stats", null);
        }
    }

    private int getCount(Connection conn, String table) {
        // Prevent SQL injection by strictly matching known table names
        if (!table.equals("users") && !table.equals("products") && !table.equals("orders") && !table.equals("reviews")) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM " + table;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            // Ignore if table doesn't exist
        }
        return 0;
    }
}
