package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.hcx.service.RedisService;
import redis.clients.jedis.JedisPool;

@Configuration
public class RedisConfiguration {

    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisService redisCache() {
        return new RedisService(redisHost, redisPort);
    }
}