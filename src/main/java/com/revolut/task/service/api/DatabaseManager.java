package com.revolut.task.service.api;

import org.jooq.DSLContext;

public interface DatabaseManager {
    DSLContext getSqlDSL();
    void close();
}
