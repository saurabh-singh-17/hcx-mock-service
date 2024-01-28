package org.swasth.hcx.controllers.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.swasth.hcx.config.AsyncConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncConfigTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(AsyncConfiguration.class);

    @Test
    void should_check_presence_of_audit_indexer_configuration_bean() {
        context.run(it -> assertThat(it).hasSingleBean(AsyncConfiguration.class));
    }
}
