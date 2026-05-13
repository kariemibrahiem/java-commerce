package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.util.ResponseUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/products/*")
public class ProductServlet extends HttpServlet {

    private ProductService productService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        try {
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.isEmpty()) {
                int id = Integer.parseInt(idParam);
                Product product = productService.getProductById(id);
                if (product != null) {
                    ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Product retrieved successfully", product);
                } else {
                    ResponseUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Product not found", null);
                }
            } else if (pathInfo == null || pathInfo.equals("/")) {
                List<Product> products = productService.getAllProducts();
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Products retrieved successfully", products);
            } else {
                int id = Integer.parseInt(pathInfo.substring(1));
                Product product = productService.getProductById(id);
                if (product != null) {
                    ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Product retrieved successfully", product);
                } else {
                    ResponseUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Product not found", null);
                }
            }
        } catch (NumberFormatException e) {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID", null);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Product newProduct = gson.fromJson(getRequestBody(req), Product.class);
            boolean success = productService.addProduct(newProduct);

            if (success) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_CREATED, "Product created successfully", newProduct);
            } else {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create product", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                Product updatedProduct = gson.fromJson(getRequestBody(req), Product.class);
                updatedProduct.setId(id);
                
                boolean success = productService.updateProduct(updatedProduct);
                if (success) {
                    ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Product updated successfully", updatedProduct);
                } else {
                    ResponseUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Product not found or update failed", null);
                }
            } catch (NumberFormatException e) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID", null);
            } catch (Exception e) {
                e.printStackTrace();
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
            }
        } else {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing product ID", null);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                boolean success = productService.deleteProduct(id);
                if (success) {
                    ResponseUtil.sendResponse(resp, HttpServletResponse.SC_OK, "Product deleted successfully", null);
                } else {
                    ResponseUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Product not found or delete failed", null);
                }
            } catch (NumberFormatException e) {
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID", null);
            } catch (Exception e) {
                e.printStackTrace();
                ResponseUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
            }
        } else {
            ResponseUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing product ID", null);
        }
    }

    private String getRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }
}
