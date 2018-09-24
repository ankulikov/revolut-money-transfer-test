package com.revolut.task.service.impl;

import com.revolut.task.service.api.DatabaseManager;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

@Slf4j
public class DatabaseManagerImpl implements DatabaseManager {
    private final DSLContext dslContext;
    private Connection connection;

    public DatabaseManagerImpl() {
        try {
            Properties properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("db/db.properties"));
            dslContext = createDSLContext(properties);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading DB connection properties", e);
        }
    }

    public DatabaseManagerImpl(Properties properties) {
        dslContext = createDSLContext(properties);
    }

    private DSLContext createDSLContext(Properties dbProps) {
        log.debug("DB properties: {}", dbProps);
        try {
            connection = DriverManager.getConnection(
                    dbProps.getProperty("jdbc.url"),
                    dbProps.getProperty("jdbc.username"),
                    dbProps.getProperty("jdbc.password"));
            return DSL.using(connection, SQLDialect.H2);
        } catch (Exception e) {
            throw new RuntimeException("Error while connecting to database", e);
        }
    }

    @Override
    public DSLContext getSqlDSL() {
        return dslContext;
    }

    @Override
    public void close() {
        try {
            connection.close();
            dslContext.close();
        } catch (Exception e) {
            log.warn("Error while closing DatabaseManager", e);
        }
    }
}
