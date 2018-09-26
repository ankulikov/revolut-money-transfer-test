package com.revolut.task.service.impl;

import com.revolut.task.model.Money;
import com.revolut.task.model.sql.tables.Account;
import com.revolut.task.service.api.AccountLockManager;
import com.revolut.task.service.api.DatabaseManager;
import com.revolut.task.service.api.ExchangeService;
import com.revolut.task.service.api.MoneyService;
import lombok.extern.slf4j.Slf4j;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

import static com.revolut.task.model.sql.Tables.ACCOUNT;

@Slf4j
//TODO: check for account lock
public class MoneyServiceImpl implements MoneyService {
    private DatabaseManager databaseManager;
    private AccountLockManager lockManager;
    private ExchangeService exchangeService;

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

    @Override
    public Money getBalance(Long id) {
        Map<String, Object> map = databaseManager.getSqlDSL()
                .select(Account.ACCOUNT.MONEY_VALUE, Account.ACCOUNT.MONEY_CURRENCY)
                .from(Account.ACCOUNT)
                .where(Account.ACCOUNT.ID.eq(id)).fetchOneMap();
        Money money = new Money();
        money.setAmount((BigDecimal) map.get(Account.ACCOUNT.MONEY_VALUE.getName()));
        money.setCurrency((String) map.get(Account.ACCOUNT.MONEY_CURRENCY.getName()));
        return money;
    }

    @Override
    public void transfer(Long fromId, Long toId, Money moneyToTransfer) {
        lockManager.doInLock(fromId, toId, () ->
                databaseManager.getSqlDSL().transaction(configuration -> {
                    withdraw(fromId, moneyToTransfer);
                    deposit(toId, moneyToTransfer);
                })
        );
    }

    @Override
    public void withdraw(Long id, Money moneyToWithdraw) {
        lockManager.doInLock(id, () ->
                databaseManager.getSqlDSL().transaction(configuration -> {
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
        lockManager.doInLock(id, () ->
                databaseManager.getSqlDSL().transaction(configuration -> {
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


}
