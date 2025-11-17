package com.iri.mktgmix.upload.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Configuration for dual datasources:
 * - PostgreSQL (Primary): Used by JPA for entity management
 * - MonetDB (Secondary): Used by JdbcTemplate for query execution
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.iri.mktgmix.upload.repository",
    entityManagerFactoryRef = "postgresEntityManagerFactory",
    transactionManagerRef = "postgresTransactionManager"
)
public class DataSourceConfig {

    // PostgreSQL DataSource Configuration
    @Value("${spring.datasource.postgres.url}")
    private String postgresUrl;

    @Value("${spring.datasource.postgres.username}")
    private String postgresUsername;

    @Value("${spring.datasource.postgres.password}")
    private String postgresPassword;

    @Value("${spring.datasource.postgres.driver-class-name}")
    private String postgresDriverClassName;

    // MonetDB DataSource Configuration
    @Value("${spring.datasource.monet.url}")
    private String monetUrl;

    @Value("${spring.datasource.monet.username}")
    private String monetUsername;

    @Value("${spring.datasource.monet.password}")
    private String monetPassword;

    @Value("${spring.datasource.monet.driver-class-name}")
    private String monetDriverClassName;

    /**
     * PostgreSQL DataSource (Primary) - Used by JPA
     */
    @Bean
    @Primary
    public DataSource postgresDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresUrl);
        config.setUsername(postgresUsername);
        config.setPassword(postgresPassword);
        config.setDriverClassName(postgresDriverClassName);
        config.setSchema("app");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        return new HikariDataSource(config);
    }

    /**
     * MonetDB DataSource (Secondary) - Used by JdbcTemplate
     */
    @Bean
    @Qualifier("monetDataSource")
    public DataSource monetDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(monetUrl);
        config.setUsername(monetUsername);
        config.setPassword(monetPassword);
        config.setDriverClassName(monetDriverClassName);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        return new HikariDataSource(config);
    }

    /**
     * EntityManagerFactory for PostgreSQL (Primary)
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(postgresDataSource())
                .packages("com.iri.mktgmix.upload.domain")
                .persistenceUnit("postgres")
                .build();
    }

    /**
     * TransactionManager for PostgreSQL (Primary)
     */
    @Bean
    @Primary
    public PlatformTransactionManager postgresTransactionManager(
            @Qualifier("postgresEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

