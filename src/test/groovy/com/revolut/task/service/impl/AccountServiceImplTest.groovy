package com.revolut.task.service.impl

import com.revolut.task.model.Account
import com.revolut.task.model.Money
import com.revolut.task.util.DatabaseMigrator
import spock.lang.Shared
import spock.lang.Specification

class AccountServiceImplTest extends Specification {
    @Shared
    def accService = new AccountServiceImpl()

    def setupSpec() {
        DatabaseMigrator.run()
        accService.setDatabaseManager(new DatabaseManagerImpl())
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
        setup:
        def accountCreated = accService.createAccount()
        def id = accountCreated.id
        expect:
        accountCreated == accService.getAccount(id)
        when:
        assert accService.removeAccount(id)
        then:
        accService.getAccount(id) == null

    }

    def "create, lock and get account"() {
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
    }

    def "delete nonexistent account"() {
        expect:
        !accService.removeAccount(1000)
    }

    def "lock nonexistent account"() {
        expect:
        !accService.lockAccount(1000)
    }
}
