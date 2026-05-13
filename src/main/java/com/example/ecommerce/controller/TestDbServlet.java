package com.example.ecommerce.controller;

import com.example.ecommerce.config.DatabaseConnection;
import com.example.ecommerce.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/api/testdb")
public class TestDbServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Database connection SUCCESSFUL", null);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database connection failed: " + e.getMessage(), null);
        }
    }
}
