package com.iri.mktgmix.upload.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuration for Flyway migrations.
 * Uses PostgreSQL DataSource (primary) for running migrations.
 * MonetDB is not used for Flyway migrations.
 */
@Configuration
public class FlywayConfig {

    /**
     * Creates Flyway bean using PostgreSQL DataSource.
     * Migrations run only on PostgreSQL, not on MonetDB.
     */
    @Bean(initMethod = "migrate")
    @Primary
    public Flyway flyway(@org.springframework.beans.factory.annotation.Qualifier("postgresDataSource") DataSource postgresDataSource) {
        return Flyway.configure()
                .dataSource(postgresDataSource)
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

