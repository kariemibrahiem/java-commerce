package com.example.ecommerce.filter;

import com.example.ecommerce.config.RedisConnection;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;

import java.io.IOException;

@WebFilter(urlPatterns = {"/api/*"})
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String clientIp = req.getRemoteAddr();
        String redisKey = "rate_limit:" + clientIp;

        try (Jedis jedis = RedisConnection.getResource()) {
            String countStr = jedis.get(redisKey);
            
            if (countStr == null) {
                // First request in the time window
                jedis.setex(redisKey, 60, "1"); // Expire in 60 seconds
            } else {
                int count = Integer.parseInt(countStr);
                if (count >= MAX_REQUESTS_PER_MINUTE) {
                    res.setStatus(429); // Too Many Requests
                    res.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
                    return; // Reject the request
                } else {
                    jedis.incr(redisKey);
                }
            }
        } catch (Exception e) {
            // Log Redis error, but allow request to pass to avoid breaking the app if Redis is down
            e.printStackTrace();
        }

        chain.doFilter(request, response);
    }
}
