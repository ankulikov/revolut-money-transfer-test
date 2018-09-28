package com.revolut.task.service.impl;

import com.google.inject.Singleton;
import com.revolut.task.model.Money;
import com.revolut.task.service.api.ExchangeService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Singleton
public class ExchangeServiceImpl implements ExchangeService {
    private Map<String, BigDecimal> rates = new HashMap<>();

    public ExchangeServiceImpl() {
        Properties properties = new Properties();
        try (InputStream ratesStream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream("exchange_rates.properties")) {
            properties.load(ratesStream);
            for (String currency : properties.stringPropertyNames()) {
                rates.put(currency, new BigDecimal(properties.getProperty(currency)));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while reading exchange rates", e);
        }
    }

    @Override
    public Money exchange(Money source, String targetCurrency) {
        BigDecimal rate = exchangeRate(source.getCurrency(), targetCurrency);
        BigDecimal newAmount = Money.multiply(source.getAmount(), rate);
        return new Money(newAmount, targetCurrency);
    }

    @Override
    public List<String> getSupportedCurrencies() {
        return new ArrayList<>(Collections.unmodifiableSet(rates.keySet()));
    }

    @Override
    public boolean isCurrencySupported(String currency) {
        return rates.containsKey(currency);
    }

    @Override
    public BigDecimal exchangeRate(String sourceCurrency, String targetCurrency) {
        BigDecimal sourceRate = rates.get(sourceCurrency);
        BigDecimal targetRate = rates.get(targetCurrency);
        if (sourceRate == null) {
            throw new IllegalArgumentException("Can't find exchange rate for " + sourceCurrency);
        }
        if (targetRate == null) {
            throw new IllegalArgumentException("Can't find exchange rate for " + targetCurrency);
        }
        return Money.divide(sourceRate, targetRate);
    }
}
