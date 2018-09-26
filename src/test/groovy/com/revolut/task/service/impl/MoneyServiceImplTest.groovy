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

    def "get balance of non-existing account"() {
        def ACC_ID = 99999L
        when:
        moneyService.getBalance(ACC_ID)
        then:
        def e = thrown(IllegalArgumentException.class)
        e.message == "Can't find account with ID=99999"
    }

    def "deposit and withdraw in the same currency"() {
        def ACC_ID = 1L //350 USD
        when:
        moneyService.deposit(1,new Money(bigDec("100"),"USD"))
        then:
        moneyService.getBalance(ACC_ID) == new Money(bigDec("450.0000"),"USD")
        when:
        moneyService.withdraw(1,new Money(bigDec("100"),"USD"))
        then:
        moneyService.getBalance(ACC_ID) == new Money(bigDec("350.0000"),"USD")
    }

    def "deposit to non-existing account"() {
        def ACC_ID = 99998L
        when:
        moneyService.deposit(ACC_ID, new Money(bigDec("100"),"RUB"))
        then:
        def e = thrown(IllegalArgumentException.class)
        e.message == "Can't find account with ID=99998"
    }

    def "deposit negative money"() {
        def ACC_ID = 1
        when:
        moneyService.deposit(ACC_ID, new Money(bigDec("-100"),"RUB"))
        then:
        def e = thrown(IllegalArgumentException.class)
        e.message == "Can't deposit negative amount of money"
    }

    def "withdraw from non-existing account"() {
        def ACC_ID = 99998L
        when:
        moneyService.withdraw(ACC_ID, new Money(bigDec("100"),"RUB"))
        then:
        def e = thrown(IllegalArgumentException.class)
        e.message == "Can't find account with ID=99998"
    }

    def "withdraw negative amount"() {
        def ACC_ID = 1
        when:
        moneyService.withdraw(ACC_ID, new Money(bigDec("-100"),"RUB"))
        then:
        def e = thrown(IllegalArgumentException.class)
        e.message == "Can't withdraw negative amount of money"
    }

    def "withdraw exceeding amount"() {
        def ACC_ID = 1 //350 USD
        when:
        moneyService.withdraw(ACC_ID, new Money(bigDec("500"),"USD"))
        then:
        def e = thrown(IllegalArgumentException.class)
        e.message == "Account ID=1 doesn't have enough money to withdraw requested amount"
    }

    def "back and forth valid transfer in the same currency"() {
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

    def "transfer negative amount"() {
        def ACC1 = 1
        def ACC2 = 2
        when:
        moneyService.transfer(ACC1, ACC2, new Money(bigDec("-50"),"EUR"))
        then:
        def e = thrown(IllegalArgumentException.class)
        e.message == "Can't transfer negative amount of money"
    }

    def "transfer exceeding amount"() {
        def ACC1 = 1 //350 USD
        def ACC2 = 2 //120 RUB
        when:
        moneyService.transfer(ACC1, ACC2, new Money(bigDec("30000"),"RUB"))
        then:
        def e = thrown(IllegalArgumentException.class)
        e.message == "Account ID=1 doesn't have enough money to withdraw requested amount"
    }
}
