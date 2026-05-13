package com.example.ecommerce.filter;

import com.example.ecommerce.util.ResponseUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = {"/api/products/*", "/api/reviews/*"})
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String method = req.getMethod();
        String uri = req.getRequestURI();
        String role = (String) req.getAttribute("role");

        // 1. Product Admin Endpoints
        if (uri.contains("/api/products") && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))) {
            if (!"ADMIN".equals(role)) {
                ResponseUtil.sendResponse(res, HttpServletResponse.SC_FORBIDDEN, "Requires ADMIN role to modify products", null);
                return;
            }
        }

        // 2. Review Endpoints
        if (uri.contains("/api/reviews") && "POST".equalsIgnoreCase(method)) {
            if (!"USER".equals(role)) {
                ResponseUtil.sendResponse(res, HttpServletResponse.SC_FORBIDDEN, "Requires USER role to post reviews", null);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
