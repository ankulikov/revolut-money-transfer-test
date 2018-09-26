package com.revolut.task.service.api;

import com.revolut.task.model.Money;

import java.math.BigDecimal;
import java.util.List;

public interface ExchangeService {
    Money exchange(Money source, String targetCurrency);
    List<String> getSupportedCurrencies();
    boolean isCurrencySupported(String currency);
    BigDecimal exchangeRate(String sourceCurrency,
                            String targetCurrency);
}
