package com.example.ecommerce.controller;

import com.example.ecommerce.dao.UserDAO;
import com.example.ecommerce.model.User;
import com.example.ecommerce.util.ResponseUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {

    private UserDAO userDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<User> users = userDAO.findAll();
            List<Map<String, Object>> safeUsers = new ArrayList<>();
            for (User user : users) {
                safeUsers.add(toSafeUser(user));
            }
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Users retrieved successfully", safeUsers);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = gson.fromJson(req.getReader(), User.class);
            if (userDAO.save(user)) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_CREATED, "User created successfully", null);
            } else {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Failed to create user", null);
            }
        } catch (Exception e) {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error", null);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = gson.fromJson(req.getReader(), User.class);
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.length() > 1) {
                user.setId(Integer.parseInt(pathInfo.substring(1)));
            }
            if (userDAO.update(user)) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "User updated successfully", null);
            } else {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Failed to update user", null);
            }
        } catch (Exception e) {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error", null);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "User ID missing", null);
                return;
            }
            int id = Integer.parseInt(pathInfo.substring(1));
            if (userDAO.delete(id)) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "User deleted successfully", null);
            } else {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, "User not found", null);
            }
        } catch (Exception e) {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error", null);
        }
    }

    private Map<String, Object> toSafeUser(User user) {
        Map<String, Object> safeUser = new LinkedHashMap<>();
        safeUser.put("id", user.getId());
        safeUser.put("username", user.getUsername());
        safeUser.put("email", user.getEmail());
        safeUser.put("role", user.getRole());
        safeUser.put("createdAt", user.getCreatedAt());
        safeUser.put("updatedAt", user.getUpdatedAt());
        return safeUser;
    }
}
