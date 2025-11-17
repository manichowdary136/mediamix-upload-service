package com.iri.mktgmix.upload.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration for MonetDB JdbcTemplate.
 * Only creates JdbcTemplate bean if one doesn't already exist (Spring Boot auto-configuration).
 */
@Configuration
public class MonetDbConfig {

    @Bean
    @ConditionalOnMissingBean(JdbcTemplate.class)
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

