package com.example.ecommerce.filter;

import com.example.ecommerce.util.ResponseUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = {
    "/api/products/*",
    "/api/reviews/*",
    "/users.html",
    "/inventory.html",
    "/create-product.html"
})
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String method = req.getMethod();
        String uri = req.getRequestURI();
        String role = (String) req.getAttribute("role");

        // 1. Product & User Admin Sections
        boolean isAdminPath = uri.contains("/users.html") || 
                             uri.contains("/inventory.html") || 
                             uri.contains("/create-product.html") ||
                             (uri.contains("/api/products") && !method.equalsIgnoreCase("GET"));

        if (isAdminPath) {
            if (!"ADMIN".equals(role)) {
                if (uri.endsWith(".html")) {
                    res.sendRedirect(req.getContextPath() + "/index.html");
                } else {
                    ResponseUtil.sendResponse(res, HttpServletResponse.SC_FORBIDDEN, "Requires ADMIN role", null);
                }
                return;
            }
        }

        // 2. Review Post (Only USER)
        if (uri.contains("/api/reviews") && "POST".equalsIgnoreCase(method)) {
            if (!"USER".equals(role)) {
                ResponseUtil.sendResponse(res, HttpServletResponse.SC_FORBIDDEN, "Requires USER role to post reviews", null);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
