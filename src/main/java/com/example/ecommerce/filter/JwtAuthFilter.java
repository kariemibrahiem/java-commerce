package com.example.ecommerce.filter;

import com.example.ecommerce.util.JwtUtil;
import com.example.ecommerce.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = {
        "/api/products/*",
        "/api/reviews/*",
        "/api/cart/*",
        "/api/orders/*",
        "/index.html",
        "/users.html",
        "/inventory.html",
        "/reviews.html",
        "/create-product.html"
})
public class JwtAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String method = req.getMethod();
        String uri = req.getRequestURI();

        // Public GET products
        boolean publicProducts =
                method.equalsIgnoreCase("GET")
                        && uri.contains("/api/products");

        String authHeader = req.getHeader("Authorization");

        // Debug
        System.out.println("AUTH HEADER = " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            try {

                String token = authHeader.substring(7).trim();

                Claims claims = JwtUtil.validateToken(token);

                Number idClaim = claims.get("id", Number.class);
                Integer userId = idClaim != null ? idClaim.intValue() : null;
                String role = claims.get("role", String.class);
                String username = claims.getSubject();

                req.setAttribute("userId", userId);
                req.setAttribute("role", role);
                req.setAttribute("username", username);

                chain.doFilter(request, response);
                return;

            } catch (Exception e) {
                e.printStackTrace();
                if (uri.endsWith(".html") || !uri.contains("/api/")) {
                    res.sendRedirect(req.getContextPath() + "/signin.html");
                } else {
                    ResponseUtil.sendResponse(res, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token", null);
                }
                return;
            }
        }

        if (publicProducts) {
            chain.doFilter(request, response);
            return;
        }

        if (uri.endsWith(".html") || !uri.contains("/api/")) {
            res.sendRedirect(req.getContextPath() + "/signin.html");
        } else {
            ResponseUtil.sendResponse(res, HttpServletResponse.SC_UNAUTHORIZED, "Missing token", null);
        }
    }
}