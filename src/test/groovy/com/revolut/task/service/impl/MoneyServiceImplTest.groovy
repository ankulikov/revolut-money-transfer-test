package com.revolut.task.service.impl


import com.revolut.task.model.Money
import com.revolut.task.util.DatabaseMigrator
import spock.lang.Shared
import spock.lang.Specification

import static com.revolut.task.TestUtils.bigDec

class MoneyServiceImplTest extends Specification {
    @Shared
    def moneyService = new MoneyServiceImpl()

    def setupSpec() {
        DatabaseMigrator.run()
        moneyService.setDatabaseManager(new DatabaseManagerImpl())
        moneyService.setLockManager(new AccountLockManagerImpl())
        moneyService.setExchangeService(new ExchangeServiceImpl())
    }

    def "get balance of existing account"() {
        expect:
        new Money(new BigDecimal("1200.0000"), "RUB") == moneyService.getBalance(2)
    }

    def "back and forth valid transfer in the same currency"() {
        setup:
        def ACC1 = 12L //1000 RUB
        def ACC2 = 11L //10 RUB
        when:
        moneyService.transfer(ACC1, ACC2, new Money(bigDec("500"), "RUB"))
        then:
        moneyService.getBalance(ACC1) == new Money(bigDec("500.0000"), "RUB")
        moneyService.getBalance(ACC2) == new Money(bigDec("510.0000"), "RUB")
        when:
        moneyService.transfer(ACC2, ACC1, new Money(bigDec("500"), "RUB"))
        then:
        moneyService.getBalance(ACC1) == new Money(bigDec("1000.0000"), "RUB")
        moneyService.getBalance(ACC2) == new Money(bigDec("10.0000"), "RUB")
    }

    def "back and forth valid transfer in foreign currency"() {
        setup:
        def ACC1 = 10L //10 EUR
        def ACC2 = 11L //10 RUB
        when:
        moneyService.transfer(ACC1, ACC2, new Money(bigDec("5"), "EUR"))
        then:
        moneyService.getBalance(ACC1) == new Money(bigDec("5.0000"), "EUR")
        moneyService.getBalance(ACC2) == new Money(bigDec("397.4225"), "RUB")
        when:
        moneyService.transfer(ACC2, ACC1, new Money(bigDec("5"), "EUR"))
        then:
        moneyService.getBalance(ACC1) == new Money(bigDec("10.0000"), "EUR")
        moneyService.getBalance(ACC2) == new Money(bigDec("10.0000"), "RUB")


    }
}
