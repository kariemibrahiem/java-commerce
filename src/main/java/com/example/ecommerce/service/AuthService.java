package com.example.ecommerce.service;

import com.example.ecommerce.dao.UserDAO;
import com.example.ecommerce.model.User;
import com.example.ecommerce.util.JwtUtil;
import com.example.ecommerce.util.PasswordUtil;

public class AuthService {
    
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public String login(String email, String password) {
        User user = userDAO.findByEmail(email);
        if (user != null && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            return JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        }
        return null; // Invalid credentials
    }

    public boolean register(String username, String email, String password) {
        if (userDAO.findByEmail(email) != null) {
            return false; // User already exists
        }
        
        String hashedPassword = PasswordUtil.hashPassword(password);
        User newUser = new User(0, username, email, hashedPassword, "USER");
        return userDAO.save(newUser);
    }
}
