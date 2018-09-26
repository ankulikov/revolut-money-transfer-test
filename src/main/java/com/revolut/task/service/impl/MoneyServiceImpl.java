package com.revolut.task.service.impl;

import com.revolut.task.model.Money;
import com.revolut.task.model.sql.tables.Account;
import com.revolut.task.service.api.*;
import lombok.extern.slf4j.Slf4j;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static com.revolut.task.model.sql.Tables.ACCOUNT;

@Slf4j
//TODO: check for account lock
public class MoneyServiceImpl implements MoneyService {
    private DatabaseManager databaseManager;
    private AccountLockManager lockManager;
    private ExchangeService exchangeService;
    private AccountService accountService;

    @Inject
    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Inject
    public void setLockManager(AccountLockManager lockManager) {
        this.lockManager = lockManager;
    }

    @Inject
    public void setExchangeService(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @Inject
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public Money getBalance(Long id) {
        Optional<Map<String, Object>> map = databaseManager.getSqlDSL()
                .select(Account.ACCOUNT.MONEY_VALUE, Account.ACCOUNT.MONEY_CURRENCY)
                .from(Account.ACCOUNT)
                .where(Account.ACCOUNT.ID.eq(id)).fetchOptionalMap();
        if (map.isPresent()) {
            Money money = new Money();
            money.setAmount((BigDecimal) map.get().get(Account.ACCOUNT.MONEY_VALUE.getName()));
            money.setCurrency((String) map.get().get(Account.ACCOUNT.MONEY_CURRENCY.getName()));
            return money;
        } else {
            throw new IllegalArgumentException("Can't find account with ID=" + id);
        }
    }

    @Override
    public void transfer(Long fromId, Long toId, Money moneyToTransfer) {
        if (moneyToTransfer.getAmount().signum() == -1) {
            throw new IllegalArgumentException("Can't transfer negative amount of money");
        }
        lockManager.doInLock(fromId, toId, () ->
                databaseManager.getSqlDSL().transaction(configuration -> {
                    checkForLock(fromId);
                    checkForLock(toId);
                    withdraw(fromId, moneyToTransfer);
                    deposit(toId, moneyToTransfer);
                })
        );
    }

    @Override
    public void withdraw(Long id, Money moneyToWithdraw) {
        if (moneyToWithdraw.getAmount().signum() == -1) {
            throw new IllegalArgumentException("Can't withdraw negative amount of money");
        }
        lockManager.doInLock(id, () ->
                databaseManager.getSqlDSL().transaction(configuration -> {
                    checkForLock(id);
                    Money balance = getBalance(id);
                    Money withdrawInCurrency =
                            exchangeService.exchange(moneyToWithdraw, balance.getCurrency());
                    Money newBalance = Money.subtract(balance, withdrawInCurrency);
                    log.trace("withdraw: id={}, old={}, withdraw={}, withdrawInCurrency={}, new={}",
                            id, balance, moneyToWithdraw, withdrawInCurrency, newBalance);
                    if (newBalance.getAmount().signum() == -1) {
                        throw new IllegalArgumentException(
                                "Account ID=" + id + " doesn't have enough money to withdraw requested amount");
                    }
                    DSL.using(configuration)
                            .update(ACCOUNT)
                            .set(ACCOUNT.MONEY_VALUE, newBalance.getAmount())
                            .set(ACCOUNT.MONEY_CURRENCY, newBalance.getCurrency())
                            .where(ACCOUNT.ID.eq(id)).execute();
                }));
    }


    @Override
    public void deposit(Long id, Money moneyToDeposit) {
        if (moneyToDeposit.getAmount().signum() == -1) {
            throw new IllegalArgumentException("Can't deposit negative amount of money");
        }
        lockManager.doInLock(id, () ->
                databaseManager.getSqlDSL().transaction(configuration -> {
                    checkForLock(id);
                    Money balance = getBalance(id);
                    Money depositInCurrency =
                            exchangeService.exchange(moneyToDeposit, balance.getCurrency());
                    Money newBalance = Money.add(balance, depositInCurrency);
                    log.trace("deposit: id={}, old={}, deposit={}, depositInCurrency={}, new={}",
                            id, balance, moneyToDeposit, depositInCurrency, newBalance);
                    DSL.using(configuration)
                            .update(ACCOUNT)
                            .set(ACCOUNT.MONEY_VALUE, newBalance.getAmount())
                            .set(ACCOUNT.MONEY_CURRENCY, newBalance.getCurrency())
                            .where(ACCOUNT.ID.eq(id)).execute();
                }));
    }

    private void checkForLock(Long accountId) {
        if (accountService.isLocked(accountId)) {
            throw new IllegalArgumentException("Account with ID=" + accountId + " is locked");
        }
    }


}
