package com.revolut.task.service.api;

import com.revolut.task.model.Money;

public interface MoneyService {
    void transfer(String fromId, String toId, Money moneyToTransfer);

    void withdraw(String id, Money moneyToWithdraw);

    void deposit(String id, Money moneyToDeposit);
}
