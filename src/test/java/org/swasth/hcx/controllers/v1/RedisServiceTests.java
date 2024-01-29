package org.swasth.hcx.controllers.v1;

import com.github.fppt.jedismock.RedisServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.service.RedisService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisServiceTests  {

    private RedisServer redisServer;
    @MockBean
    private RedisService redis;

    private RedisService invalidRedis;

    @BeforeAll
    void setup() throws IOException {
        redisServer = RedisServer.newRedisServer().start();
        redis = new RedisService(redisServer.getHost(), redisServer.getBindPort());
        invalidRedis = new RedisService("redisPort", 6370);
    }

    @AfterAll
    void shutdown() throws IOException {
        redisServer.stop();
    }

    @Test
    void testSet() throws Exception {
        redis.set("test", "test", 10000000);
    }

    @Test
    void testGet() throws Exception {
        redis.get("test");
    }


    @Test
    void testGetException() {
        Exception exception = assertThrows(Exception.class, () -> invalidRedis.get("exception"));
        assertTrue(exception.getMessage().contains("Exception Occurred While Fetching Data from Redis Cache for Key : exception"));
    }

    @Test
    void testSetException() {
        Exception exception = assertThrows(Exception.class, () -> invalidRedis.set("exception","123",10000000));
        assertTrue(exception.getMessage().contains("Exception Occurred While Saving Data to Redis Cache for Key : exception"));
    }
}
