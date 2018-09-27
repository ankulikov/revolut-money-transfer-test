package com.revolut.task.e2e

import com.revolut.task.AppStarter
import com.revolut.task.model.Account
import com.revolut.task.model.Error
import com.revolut.task.model.Money
import com.revolut.task.util.DatabaseMigrator
import org.eclipse.jetty.server.Server
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.NotFoundException
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType

import static com.revolut.task.TestUtils.bigDec

class E2ETests extends Specification {
    static final int PORT = 32758
    private static final String REST_URI = "http://localhost:${PORT}/api"
    @Shared
    Server server = null
    @Shared
    private def client = ClientBuilder.newClient()

    def "create, get, lock, unlock, delete account"() {
        when:
        def account = createAccount(null)
        then:
        account.balance == new Money(bigDec("0.0000"), "USD")
        when:
        client.target(REST_URI).path("account/${account.id}/lock").request().method("POST")
        then:
        getAccount(account.id).locked
        when:
        client.target(REST_URI).path("account/${account.id}/unlock").request().method("POST")
        then:
        !getAccount(account.id).locked
        when:
        client.target(REST_URI).path("account/${account.id}").request().delete()
        getAccount(account.id)
        then:
        NotFoundException ex = thrown()
        ex.getResponse().readEntity(Error.class).error == "Can't find account with ID=${account.id}"
    }


    def "create, deposit, withdraw, transfer"() {
        def acc1 = createAccount("RUB")
        def acc2 = createAccount("EUR")
        when:
        deposit(acc1.id, new Money(bigDec("10000.0000"), "RUB"))
        deposit(acc2.id, new Money(bigDec("10000.0000"), "RUB"))
        then:
        getAccount(acc1.id).balance == new Money(bigDec("10000.0000"), "RUB")
        getAccount(acc2.id).balance == new Money(bigDec("129.0000"), "EUR")
        when:
        withdraw(acc1.id, new Money(bigDec("100.5000"), "USD"))
        withdraw(acc2.id, new Money(bigDec("100.4000"), "EUR"))
        then:
        getAccount(acc1.id).balance == new Money(bigDec("3367.0000"), "RUB")
        getAccount(acc2.id).balance == new Money(bigDec("28.6000"), "EUR")
        when:
        transfer(acc1.id, acc2.id, new Money(bigDec("3000"), "RUB"))
        then:
        getAccount(acc1.id).balance == new Money(bigDec("367.0000"), "RUB")
        getAccount(acc2.id).balance == new Money(bigDec("67.3000"), "EUR")
    }


    protected Account createAccount(String currency) {
        return client.target(REST_URI)
                .path("account").queryParam("currency", currency)
                .request().method("POST").readEntity(Account.class)
    }

    protected void deposit(Long id, Money money) {
        client.target(REST_URI).path("account/${id}/deposit").request().post(Entity.json(money))
    }

    protected void withdraw(Long id, Money money) {
        client.target(REST_URI).path("account/${id}/withdraw").request().post(Entity.json(money))
    }

    protected void transfer(Long fromId, Long toId, Money money) {
        client.target(REST_URI)
                .path("account/${fromId}/transfer/${toId}")
                .request().post(Entity.json(money))

    }

    protected Account getAccount(long id) {
        return client.target(REST_URI).path("account/${id}").request(MediaType.APPLICATION_JSON).get(Account.class)
    }

    def setupSpec() {
        DatabaseMigrator.run()
        server = AppStarter.startServer(PORT, false)
    }

    def cleanupSpec() {
        try {
            server.stop()
        } finally {
            server.destroy()
        }
    }
}
