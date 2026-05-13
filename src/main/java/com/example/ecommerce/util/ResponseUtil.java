package com.example.ecommerce.util;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ResponseUtil {
    private static final Gson gson = new Gson();

    public static void sendResponse(HttpServletResponse resp, int status, String msg, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        if (data == null) {
            data = new java.util.ArrayList<>();
        }
        
        ApiResponse<Object> response = new ApiResponse<>(status, msg, data);
        resp.getWriter().write(gson.toJson(response));
    }
}
