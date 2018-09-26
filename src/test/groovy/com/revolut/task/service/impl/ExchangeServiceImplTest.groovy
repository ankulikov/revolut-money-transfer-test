package com.revolut.task.service.impl

import com.revolut.task.TestUtils
import com.revolut.task.model.Money
import spock.lang.Shared
import spock.lang.Specification

//TODO: add tests for negative amount
class ExchangeServiceImplTest extends Specification {
    @Shared
    def converter = new ExchangeServiceImpl()

    def "exchange rate for existing currencies"() {
        expect:
        converter.exchangeRate("EUR", "RUB") == 77.4845
    }

    def "exchange rate for nonexistent currency"() {
        when:
        converter.exchangeRate("EUR", "XXX")
        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Can't find exchange rate for XXX"
    }

    def "exchange money in existing currencies"() {
        when:
        def money = converter.exchange(
                new Money(TestUtils.bigDec("10"), "EUR"), "RUB")
        then:
        money.currency == "RUB"
        money.amount == 774.8450
    }

    def "exchange money in nonexistent currencies"() {
        when:
        converter.exchange(
                new Money(TestUtils.bigDec("10"), "EUR"), "YYY")
        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Can't find exchange rate for YYY"
    }

    def "exchange money to same currency"() {
        when:
        def money = converter.exchange(
                new Money(TestUtils.bigDec("5"), "EUR"), "EUR")
        then:
        money.currency == "EUR"
        money.amount == 5
    }

    def "check all supported currencies"() {
        when:
        def currencies = converter.getSupportedCurrencies()
        then:
        currencies.containsAll(["USD", "RUB", "EUR", "GBP", "INR"])
    }

    def "check for currency support"() {
        expect:
        converter.isCurrencySupported("RUB")
        !converter.isCurrencySupported("XXX")
    }

}
