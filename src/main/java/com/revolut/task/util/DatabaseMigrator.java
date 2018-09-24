package com.revolut.task.util;

import org.flywaydb.core.Flyway;

import java.util.Properties;

/**
 * Fill up database with recent DDL & DML scripts from 'db/migration/*' resources
 */
public class DatabaseMigrator {
    public static void main(String[] args) {
        run();
    }

    public static void run() {
        Properties props = Utils.loadJdbcProperties();
        Flyway flyway = new Flyway();
        flyway.setDataSource(
                props.getProperty("jdbc.url"),
                props.getProperty("jdbc.username"),
                props.getProperty("jdbc.password"));
        flyway.migrate();
    }
}
