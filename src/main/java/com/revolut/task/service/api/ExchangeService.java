package com.revolut.task.service.api;

import com.revolut.task.model.Money;

import java.math.BigDecimal;

public interface ExchangeService {
    Money exchange(Money source, String targetCurrency);
    BigDecimal exchangeRate(String sourceCurrency,
                            String targetCurrency);
}
