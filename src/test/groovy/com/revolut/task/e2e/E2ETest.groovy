package com.revolut.task.e2e

import com.revolut.task.AppStarter
import com.revolut.task.model.Account
import com.revolut.task.model.Error
import com.revolut.task.model.Money
import com.revolut.task.util.DatabaseMigrator
import org.apache.log4j.Level
import org.eclipse.jetty.server.Server
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.BadRequestException
import javax.ws.rs.NotAllowedException
import javax.ws.rs.NotFoundException
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import java.util.concurrent.Executors

import static com.revolut.task.TestUtils.bigDec
import static org.apache.log4j.LogManager.getRootLogger

class E2ETest extends Specification {
    static final int PORT = 32758
    private static final String REST_URI = "http://localhost:${PORT}/api"
    @Shared
    Server server = null
    @Shared
    private def client = ClientBuilder.newClient()
    @Shared
    def taskExecutor = Executors.newFixedThreadPool(10)

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

    def "non-existed URI"() {
        NotFoundException ex
        when:
        client.target(REST_URI).path("some_path").request().get(Error.class)
        then:
        ex = thrown()
        ex.getResponse().readEntity(Error.class).error == "Can't find requested resource"
        when:
        client.target(REST_URI).path("account/NOT_A_NUMBER").request().get(Account.class)
        then:
        ex = thrown()
        ex.getResponse().readEntity(Error.class).error == "Can't find requested resource"
    }

    def "incorrect http method"() {
        NotAllowedException ex
        when:
        client.target(REST_URI).path("account/1").request().method("POST", Error.class)
        then:
        ex = thrown()
        ex.getResponse().readEntity(Error.class).error == "Requested resource is not allowed by specified method"
    }

    def "invalid json request"() {
        BadRequestException ex
        def acc = createAccount(null)
        when:
        client.target(REST_URI).path("account/${acc.id}/deposit").request().post(Entity.json("{\"some field\": true}"), Error.class)
        then:
        ex = thrown()
        ex.getResponse().readEntity(Error.class).error == "Invalid JSON request"
    }

    def "load testing"() {
        def acc1 = createAccount("RUB")
        def acc2 = createAccount("RUB")
        deposit(acc1.id, new Money(bigDec("200000.0000"), "RUB"))
        deposit(acc2.id, new Money(bigDec("300000.0000"), "RUB"))
        def tasks = []
        def oldRootLevel = rootLogger.getLevel()
        rootLogger.setLevel(Level.OFF) //to disable console output
        when:
        1000.times {
            def iter = it
            tasks << { withdraw(acc1.id, new Money(bigDec("5"), "RUB")); println "withdraw-${iter}" }
            tasks << { transfer(acc2.id, acc1.id, new Money(bigDec("5"), "RUB")); println "transfer-${iter}" }
            tasks << { deposit(acc2.id, new Money(bigDec("5"), "RUB")); println "deposit-${iter}" }
        }
        println "tasks were created"
        taskExecutor.invokeAll(tasks).forEach({ it.get() })
        then:
        getAccount(acc1.id).balance == new Money(bigDec("200000.0000"), "RUB")
        getAccount(acc2.id).balance == new Money(bigDec("300000.0000"), "RUB")
        cleanup:
        rootLogger.setLevel(oldRootLevel)
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
