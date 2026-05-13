package com.example.ecommerce.filter;

import com.example.ecommerce.util.JwtUtil;
import com.example.ecommerce.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = {"/api/products/*", "/api/reviews/*", "/api/cart/*", "/api/order/*"})
public class JwtAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Note: GET /api/products is public, but we can still extract user if token is provided.
        // If token is missing and it's GET /api/products, allow it.
        boolean isGetProducts = "GET".equalsIgnoreCase(req.getMethod()) && req.getRequestURI().contains("/api/products");

        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.trim().startsWith("Bearer")) {
            String token = authHeader.trim().replaceFirst("^Bearer\\s*", "").trim();
            try {
                Claims claims = JwtUtil.validateToken(token);
                if (claims != null) {
                    req.setAttribute("userId", claims.get("id", Integer.class));
                    req.setAttribute("username", claims.getSubject());
                    req.setAttribute("role", claims.get("role", String.class));
                    
                    chain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                // Token invalid
                e.printStackTrace();
                req.setAttribute("jwtError", e.getMessage());
            }
        }

        if (isGetProducts) {
            // Allow public access to GET products without token
            chain.doFilter(request, response);
        } else {
            String errorMsg = (String) req.getAttribute("jwtError");
            if (errorMsg == null) errorMsg = "Missing or invalid token";
            ResponseUtil.sendResponse(res, HttpServletResponse.SC_UNAUTHORIZED, "Auth failed: " + errorMsg, null);
        }
    }
}
