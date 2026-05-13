package com.example.ecommerce.service;

import com.example.ecommerce.config.RedisConnection;
import com.example.ecommerce.dao.ProductDAO;
import com.example.ecommerce.model.Product;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

import java.util.List;

public class ProductService {

    private final ProductDAO productDAO;
    private final Gson gson;
    private static final String REDIS_PRODUCT_KEY = "product:";

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.gson = new Gson();
    }

    public List<Product> getAllProducts() {
        return productDAO.findAll(); // Could also cache the entire list if needed
    }

    public Product getProductById(int id) {
        try (Jedis jedis = RedisConnection.getResource()) {
            String cacheKey = REDIS_PRODUCT_KEY + id;
            String cachedProduct = jedis.get(cacheKey);

            if (cachedProduct != null) {
                // Cache hit
                return gson.fromJson(cachedProduct, Product.class);
            } else {
                // Cache miss, fetch from DB
                Product product = productDAO.findById(id);
                if (product != null) {
                    jedis.setex(cacheKey, 3600, gson.toJson(product)); // Cache for 1 hour
                }
                return product;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to DB if Redis fails
            return productDAO.findById(id);
        }
    }

    public boolean addProduct(Product product) {
        return productDAO.save(product);
    }

    public boolean updateProduct(Product product) {
        boolean updated = productDAO.update(product);
        if (updated) {
            // Invalidate or update cache
            try (Jedis jedis = RedisConnection.getResource()) {
                jedis.del(REDIS_PRODUCT_KEY + product.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return updated;
    }

    public boolean deleteProduct(int id) {
        boolean deleted = productDAO.delete(id);
        if (deleted) {
            try (Jedis jedis = RedisConnection.getResource()) {
                jedis.del(REDIS_PRODUCT_KEY + id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deleted;
    }
}
