package com.revolut.task.service.impl

import com.google.inject.Guice
import com.google.inject.Injector
import com.revolut.task.di.AppInjectorModule
import com.revolut.task.model.Money
import com.revolut.task.model.exceptions.AccountLockedException
import com.revolut.task.model.exceptions.AccountNotFoundException
import com.revolut.task.model.exceptions.MoneyNegativeAmountException
import com.revolut.task.model.exceptions.NotEnoughMoneyException
import com.revolut.task.service.api.MoneyService
import com.revolut.task.util.DatabaseMigrator
import spock.lang.Shared
import spock.lang.Specification

import static com.revolut.task.TestUtils.bigDec

class MoneyServiceImplTest extends Specification {
    @Shared
    MoneyService moneyService = null

    def setupSpec() {
        DatabaseMigrator.run()
        Injector injector = Guice.createInjector(new AppInjectorModule())
        moneyService = injector.getInstance(MoneyService.class)
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
        AccountNotFoundException e = thrown()
        e.message == "Can't find account with ID=99999"
    }

    def "deposit and withdraw in the same currency"() {
        def ACC_ID = 1L //350 USD
        when:
        moneyService.deposit(1, new Money(bigDec("100"), "USD"))
        then:
        moneyService.getBalance(ACC_ID) == new Money(bigDec("450.0000"), "USD")
        when:
        moneyService.withdraw(1, new Money(bigDec("100"), "USD"))
        then:
        moneyService.getBalance(ACC_ID) == new Money(bigDec("350.0000"), "USD")
    }

    def "deposit to non-existing account"() {
        def ACC_ID = 99998L
        when:
        moneyService.deposit(ACC_ID, new Money(bigDec("100"), "RUB"))
        then:
        AccountNotFoundException e = thrown()
        e.message == "Can't find account with ID=99998"
    }

    def "deposit negative money"() {
        def ACC_ID = 1L
        when:
        moneyService.deposit(ACC_ID, new Money(bigDec("-100"), "RUB"))
        then:
        MoneyNegativeAmountException e = thrown()
        e.message == "Can't deposit negative amount of money"
    }

    def "deposit from locked account"() {
        def ACC_ID = 3L //1300 EUR, locked
        when:
        moneyService.deposit(ACC_ID, new Money(bigDec("10"), "EUR"))
        then:
        AccountLockedException e = thrown()
        e.message == "Account with ID=3 is locked"
    }

    def "withdraw from non-existing account"() {
        def ACC_ID = 99998L
        when:
        moneyService.withdraw(ACC_ID, new Money(bigDec("100"), "RUB"))
        then:
        AccountNotFoundException e = thrown()
        e.message == "Can't find account with ID=99998"
    }

    def "withdraw negative amount"() {
        def ACC_ID = 1L
        when:
        moneyService.withdraw(ACC_ID, new Money(bigDec("-100"), "RUB"))
        then:
        MoneyNegativeAmountException e = thrown()
        e.message == "Can't withdraw negative amount of money"
    }

    def "withdraw exceeding amount"() {
        def ACC_ID = 1L //350 USD
        when:
        moneyService.withdraw(ACC_ID, new Money(bigDec("500"), "USD"))
        then:
        NotEnoughMoneyException e = thrown()
        e.message == "Account with ID=1 doesn't have enough money to complete withdraw"
    }

    def "withdraw from locked account"() {
        def ACC_ID = 3 //1300 EUR, locked
        when:
        moneyService.withdraw(ACC_ID, new Money(bigDec("10"), "EUR"))
        then:
        AccountLockedException e = thrown()
        e.message == "Account with ID=3 is locked"
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
        moneyService.transfer(ACC1, ACC2, new Money(bigDec("-50"), "EUR"))
        then:
        MoneyNegativeAmountException e = thrown()
        e.message == "Can't transfer negative amount of money"
    }

    def "transfer exceeding amount"() {
        def ACC1 = 1 //350 USD
        def ACC2 = 2 //120 RUB
        when:
        moneyService.transfer(ACC1, ACC2, new Money(bigDec("30000"), "RUB"))
        then:
        NotEnoughMoneyException e = thrown()
        e.message == "Account with ID=1 doesn't have enough money to complete withdraw"
    }

    def "transfer to locked account"() {
        def ACC1 = 1L //350 USD
        def ACC2 = 3L //1300 EUR, locked
        when:
        moneyService.transfer(ACC1, ACC2, new Money(bigDec("20"), "RUB"))
        then:
        AccountLockedException e = thrown()
        e.message == "Account with ID=3 is locked"
    }
}
