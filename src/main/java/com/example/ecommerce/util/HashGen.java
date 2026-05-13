package com.example.ecommerce.util;

public class HashGen {
    public static void main(String[] args) {
        String hashed = PasswordUtil.hashPassword("admin123");
        System.out.println("HASH FOR admin123: " + hashed);
        System.out.println("CHECK: " + PasswordUtil.checkPassword("admin123", "$2a$10$w4rU.5zXo1xMv0N1hJzDMeCj.2TfI/N2t4L.fGj5rW1gD4w1Rj7sK"));
    }
}
