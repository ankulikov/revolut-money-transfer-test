package com.revolut.task.service.impl

import org.jooq.exception.DataAccessException
import spock.lang.Specification

class DatabaseManagerImplTest extends Specification {

    def "create from explicit properties"() {
        setup:
        def props = new Properties()
        props.setProperty("jdbc.url","jdbc:h2:mem:test")
        def dbManager = new DatabaseManagerImpl(props)
        when:
        String res = dbManager.sqlDSL.fetchValue(
                "SELECT 'STRING_FROM_DB' FROM DUAL")
        then:
        res == "STRING_FROM_DB"
        cleanup:
        dbManager.close()
    }

    def "create from file properties"() {
        setup:
        def dbManager = new DatabaseManagerImpl()
        when:
        String res = dbManager.sqlDSL.fetchValue(
                "SELECT 'STRING_FROM_DB' FROM DUAL")
        then:
        res == "STRING_FROM_DB"
        cleanup:
        dbManager.close()
    }

    def "try to use after close"() {
        setup:
        def dbManager = new DatabaseManagerImpl()
        when:
        dbManager.close()
        dbManager.sqlDSL.fetchValue("SELECT 1 FROM DUAL")
        then:
        thrown(DataAccessException.class)
    }

}
