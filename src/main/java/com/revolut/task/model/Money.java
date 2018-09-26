package com.revolut.task.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Money {
    private BigDecimal amount;
    private String currency;

    public static Money add(Money first, Money second) {
        if (!first.getCurrency().equals(second.currency)) {
            throw new IllegalArgumentException("Can't sum money in different currencies");
        }
        Money money = new Money(first.amount.add(second.amount), first.currency);
        money.amount = money.amount.setScale(4, RoundingMode.HALF_UP);
        return money;
    }

    public static Money subtract(Money first, Money second) {
        if (!first.getCurrency().equals(second.currency)) {
            throw new IllegalArgumentException("Can't sum money in different currencies");
        }
        Money money = new Money(first.amount.subtract(second.amount), first.currency);
        money.amount = money.amount.setScale(4, RoundingMode.HALF_UP);
        return money;
    }

    public static BigDecimal divide(BigDecimal firstAmount, BigDecimal secondAmount) {
        BigDecimal newAmount = firstAmount.divide(secondAmount, RoundingMode.HALF_UP);
        newAmount = newAmount.setScale(4, RoundingMode.HALF_UP);
        return newAmount;
    }

    public static BigDecimal multiply(BigDecimal firstAmount, BigDecimal secondAmount) {
        BigDecimal newAmount = firstAmount.multiply(secondAmount);
        newAmount = newAmount.setScale(4, RoundingMode.HALF_UP);
        return newAmount;
    }
}
