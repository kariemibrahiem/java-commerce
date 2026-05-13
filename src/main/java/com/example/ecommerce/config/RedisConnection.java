package com.example.ecommerce.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection {

    private static JedisPool jedisPool;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(2);
        
        // Connect to local Redis server on default port 6379
        jedisPool = new JedisPool(poolConfig, "localhost", 6379);
    }

    private RedisConnection() {}

    public static Jedis getResource() {
        return jedisPool.getResource();
    }
}
