package com.example.ecommerce.controller;

import com.example.ecommerce.model.User;
import com.example.ecommerce.service.AuthService;
import com.example.ecommerce.util.JwtUtil;
import com.example.ecommerce.util.PasswordUtil;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private AuthService authService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        JsonObject jsonBody = gson.fromJson(buffer.toString(), JsonObject.class);

        if ("/register".equals(pathInfo)) {
            handleRegister(jsonBody, resp);
        } else if ("/login".equals(pathInfo)) {
            handleLogin(req, jsonBody, resp);
        } else {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found", null);
        }
    }

    private void handleRegister(JsonObject jsonBody, HttpServletResponse resp) throws IOException {
        if (!jsonBody.has("username") || !jsonBody.has("email") || !jsonBody.has("password")) {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing fields", null);
            return;
        }

        String username = jsonBody.get("username").getAsString();
        String email = jsonBody.get("email").getAsString();
        String password = jsonBody.get("password").getAsString();

        try {
            boolean success = authService.register(username, email, password);
            if (success) {
                // Fetch the newly created user to return in the data object
                User newUser = authService.getUserDAO().findByEmail(email);
                
                Map<String, Object> data = new HashMap<>();
                data.put("user", toSafeUser(newUser));
                
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_CREATED, "User registered successfully", data);
            } else {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_CONFLICT, "User already exists", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
        }
    }

    private void handleLogin(HttpServletRequest req, JsonObject jsonBody, HttpServletResponse resp) throws IOException {
        if (!jsonBody.has("email") || !jsonBody.has("password")) {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing email or password", null);
            return;
        }

        String email = jsonBody.get("email").getAsString();
        String password = jsonBody.get("password").getAsString();

        try {
            User user = authService.getUserDAO().findByEmail(email);

            if (user != null && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
                String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                
                Map<String, Object> data = new HashMap<>();
                data.put("token", "Bearer " + token);
                data.put("user", toSafeUser(user));

                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "login successfully", data);
            } else {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
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
