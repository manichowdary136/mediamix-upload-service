package com.iri.mktgmix.upload.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuration for Flyway migrations.
 * Only enables Flyway when connecting to PostgreSQL, skips for MonetDB.
 * 
 * This configuration class is only loaded when the datasource URL contains 'postgresql'.
 * When loaded, it creates a Flyway bean and runs migrations.
 * For MonetDB connections, this configuration won't be loaded, and Flyway will remain
 * disabled (as set in application.properties: spring.flyway.enabled=false).
 */
@Configuration
@ConditionalOnExpression("'${spring.datasource.url:}'.contains('postgresql')")
public class FlywayConfig {

    /**
     * Creates Flyway bean only for PostgreSQL.
     * This bean is only created when datasource URL contains 'postgresql'.
     */
    @Bean(initMethod = "migrate")
    @Primary
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .defaultSchema("app")
                .schemas("app", "meta")
                .locations("classpath:db/migration")
                .load();
    }

    /**
     * Flyway migration initializer for PostgreSQL.
     */
    @Bean
    @DependsOn("flyway")
    public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway);
    }
}

