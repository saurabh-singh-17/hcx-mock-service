package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.service.PostgresService;

import java.sql.SQLException;

@Configuration
public class PostgresConfiguration {

    @Value("${postgres.url}")
    private String postgresUrl;

    @Value("${postgres.user}")
    private String postgresUser;

    @Value("${postgres.password}")
    private String postgresPassword;

    @Bean
    public PostgresService postgresService() throws SQLException, ClientException {
        return new PostgresService(postgresUrl, postgresUser, postgresPassword);
    }

}
