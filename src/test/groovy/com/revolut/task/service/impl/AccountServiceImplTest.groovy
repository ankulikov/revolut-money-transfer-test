package com.revolut.task.service.impl

import com.revolut.task.model.Account
import com.revolut.task.model.Money
import com.revolut.task.model.exceptions.AccountNotFoundException
import com.revolut.task.util.DatabaseMigrator
import spock.lang.Shared
import spock.lang.Specification

class AccountServiceImplTest extends Specification {
    @Shared
    def accService = new AccountServiceImpl()

    def setupSpec() {
        DatabaseMigrator.run()
        accService.setDatabaseManager(new DatabaseManagerImpl())
        accService.setExchangeService(new ExchangeServiceImpl())
    }

    def "create new account"() {
        when:
        def account = accService.createAccount()
        then:
        account.id != 0
        !account.locked
        account.balance.amount == 0
        account.balance.currency == "USD"
    }

    def "create new account with supported currency"() {
        when:
        def account = accService.createAccount("RUB")
        then:
        account.id != 0
        !account.locked
        account.balance.amount == 0
        account.balance.currency == "RUB"
    }

    def "create new account with unsupported currency"() {
        when:
        accService.createAccount("XYZ")
        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Currency XYZ is not supported"

    }

    def "get existing account"() {
        setup:
        def expectedAccount = new Account()
        expectedAccount.with {
            id = 1
            locked = false
            balance = new Money()
            balance.with {
                amount = new BigDecimal("350.0000")
                currency = "USD"
            }
        }
        expect:
        expectedAccount == accService.getAccount(1)
    }

    def "create, get and delete existing account"() {
        def accountCreated = accService.createAccount()
        def id = accountCreated.id
        expect:
        accountCreated == accService.getAccount(id)
        accService.removeAccount(id)
        when:
        accService.getAccount(id)
        then:
        AccountNotFoundException ex = thrown()
        ex.message == "Can't find account with ID="+id

    }

    def "create, lock, unlock and get account"() {
        when:
        def account = accService.createAccount()
        def id = account.id
        then:
        !account.locked
        when:
        accService.lockAccount(id)
        account = accService.getAccount(id)
        then:
        account.locked
        when:
        accService.unlockAccount(id)
        account = accService.getAccount(id)
        then:
        !account.locked
    }

    def "delete nonexistent account"() {
        when:
        accService.removeAccount(1000L)
        then:
        AccountNotFoundException ex = thrown()
        ex.message == "Can't find account with ID=1000"
    }

    def "lock nonexistent account"() {
        when:
        accService.lockAccount(1000L)
        then:
        AccountNotFoundException ex = thrown()
        ex.message == "Can't find account with ID=1000"
    }
}
