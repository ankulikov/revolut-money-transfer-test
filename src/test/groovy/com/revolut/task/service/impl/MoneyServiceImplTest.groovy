package com.revolut.task.service.impl

import com.revolut.task.model.Money
import com.revolut.task.util.DatabaseMigrator
import spock.lang.Shared
import spock.lang.Specification

class MoneyServiceImplTest extends Specification {
    @Shared
    def moneyService = new MoneyServiceImpl()

    def setupSpec() {
        DatabaseMigrator.run()
        moneyService.setDatabaseManager(new DatabaseManagerImpl())
    }

    def "get balance of existing account"() {
        expect:
        new Money(new BigDecimal("1200.0000"),"RUB") == moneyService.getBalance(2)
    }


}
