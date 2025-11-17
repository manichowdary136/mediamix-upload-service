package com.iri.mktgmix.upload.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration for MonetDB JdbcTemplate.
 * Creates JdbcTemplate bean using MonetDB DataSource.
 */
@Configuration
public class MonetDbConfig {

    @Bean
    @Qualifier("monetJdbcTemplate")
    public JdbcTemplate monetJdbcTemplate(@Qualifier("monetDataSource") DataSource monetDataSource) {
        return new JdbcTemplate(monetDataSource);
    }
}

