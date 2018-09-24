package com.revolut.task.service.impl

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
}
