package com.revolut.task.service.impl;

import com.revolut.task.model.Money;
import com.revolut.task.service.api.ExchangeService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
        return new Money(
                source.getAmount().multiply(rate),
                targetCurrency);
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
        return sourceRate.divide(targetRate, 5, RoundingMode.HALF_UP);
    }
}
