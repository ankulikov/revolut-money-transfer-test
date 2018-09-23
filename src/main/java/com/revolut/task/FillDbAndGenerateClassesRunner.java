package com.revolut.task;

import org.flywaydb.core.Flyway;
import org.h2.Driver;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.h2.H2Database;
import org.jooq.meta.jaxb.*;

public class FillDbAndGenerateClassesRunner {
    public static void main(String[] args) throws Exception {
        Jdbc jdbc = new Jdbc()
                .withDriver(Driver.class.getName())
                .withUrl("jdbc:h2:./revolut_db");
        //1. fill up database with recent DDL & DML scripts
        Flyway flyway = new Flyway();
        flyway.setDataSource(jdbc.getUrl(), jdbc.getUser(), jdbc.getPassword());
        flyway.migrate();
        //2. generate classes by DB structure
        Configuration configuration = new Configuration();
        configuration
                .withJdbc(jdbc)
                .withGenerator(new Generator()
                        .withDatabase(new Database()
                                .withName(H2Database.class.getName())
                                .withInputSchema("PUBLIC")
                               .withExcludes("INFORMATION_SCHEMA.*|FLYWAY.*"))
                        .withTarget(new Target()
                                .withPackageName("com.revolut.task.model.sql")
                                .withDirectory("./src/main/java")));
        (new GenerationTool()).run(configuration);


    }
}
