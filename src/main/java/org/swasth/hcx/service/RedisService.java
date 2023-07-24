package org.swasth.hcx.service;

import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.exception.ErrorCodes;
import org.swasth.hcx.exception.ServerException;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.JSONUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;

public class RedisService {

    private final String redisHost;
    private final int redisPort;

    public RedisService(String redisHost, int redisPort) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
    }

    public Jedis getConnection() throws Exception {
        try {
            return new Jedis(redisHost, redisPort);
        } catch (Exception e) {
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, "Error connecting to redis server " + e);
        }
    }

    public void set(String key, String value, int ttl) throws Exception {
        try (Jedis jedis = getConnection()) {
            jedis.setex(key, ttl, value);
        } catch (Exception e) {
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, "Exception Occurred While Saving Data to Redis Cache for Key : " + key + "| Exception is:" + e);
        }
    }

    public List<Map<String, Object>> get(String key) throws Exception {
        try (Jedis jedis = getConnection()) {
            Set<String> matchingKeys = jedis.keys("*" + key + "*");
            if (matchingKeys.isEmpty()) {
                throw new ClientException("There is no value present with key " + key);
            }
            List<String> sortedKeys = new ArrayList<>(matchingKeys);
            sortedKeys.sort((k1, k2) -> (int) (jedis.ttl(k2) - jedis.ttl(k1)));
            List<String> matchingValues = jedis.mget(sortedKeys.toArray(new String[0]));
            List<Map<String, Object>> notificationList = new ArrayList<>();
            for (String keys : matchingValues) {
                notificationList.add(JSONUtils.deserialize(keys, Map.class));
            }
            return notificationList;
        } catch (Exception e) {
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, "Exception Occurred While Fetching Data from Redis Cache for Key : " + key + "| Exception is:" + e.getMessage());
        }
    }
}