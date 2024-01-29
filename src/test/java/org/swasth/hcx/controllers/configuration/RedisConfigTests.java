package org.swasth.hcx.controllers.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.swasth.hcx.config.RedisConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisConfigTests {

    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(RedisConfiguration.class);

    @Test
    void should_check_presence_of_redis_configuration_bean() {
        context.run(it -> assertThat(it).hasSingleBean(RedisConfiguration.class));
    }
}
