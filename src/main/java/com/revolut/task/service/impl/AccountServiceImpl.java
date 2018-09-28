package com.revolut.task.service.impl;

import com.google.inject.Singleton;
import com.revolut.task.model.Account;
import com.revolut.task.model.Money;
import com.revolut.task.model.exceptions.AccountNotFoundException;
import com.revolut.task.model.sql.tables.records.AccountRecord;
import com.revolut.task.service.api.AccountService;
import com.revolut.task.service.api.DatabaseManager;
import com.revolut.task.service.api.ExchangeService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;

import javax.inject.Inject;

import static com.revolut.task.model.sql.Tables.ACCOUNT;

@Singleton
public class AccountServiceImpl implements AccountService {
    private DatabaseManager databaseManager;
    private ExchangeService exchangeService;


    @Inject
    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Inject
    public void setExchangeService(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @Override
    public Account createAccount() {
        AccountRecord record = databaseManager.getSqlDSL()
                .insertInto(ACCOUNT)
                .defaultValues()
                .returning().fetchOne();
        return convertFrom(record);
    }

    @Override
    public Account createAccount(String currency) {
        if (!exchangeService.isCurrencySupported(currency)) {
            throw new IllegalArgumentException("Currency " + currency + " is not supported");
        }
        AccountRecord record = databaseManager.getSqlDSL()
                .insertInto(ACCOUNT, ACCOUNT.MONEY_CURRENCY)
                .values(currency)
                .returning().fetchOne();
        return convertFrom(record);
    }

    @Override
    public Account getAccount(Long id) {
        AccountRecord accountRecord = databaseManager.getSqlDSL()
                .selectFrom(ACCOUNT)
                .where(ACCOUNT.ID.eq(id))
                .fetchOne();
        if (accountRecord != null) {
            return convertFrom(accountRecord);
        }
        throw new AccountNotFoundException(id);
    }

    @Override
    public boolean isLocked(Long id) {
        Record1<Boolean> locked = databaseManager.getSqlDSL()
                .select(ACCOUNT.LOCKED).from(ACCOUNT)
                .where(ACCOUNT.ID.eq(id))
                .fetchOne();
        if (locked == null) {
            throw new AccountNotFoundException(id);
        }
        return locked.get(ACCOUNT.LOCKED);
    }

    @Override
    public void removeAccount(Long id) {
        boolean accountExists = databaseManager.getSqlDSL()
                .deleteFrom(ACCOUNT)
                .where(ACCOUNT.ID.eq(id))
                .execute() > 0;
        if (!accountExists) {
            throw new AccountNotFoundException(id);
        }
    }

    @Override
    public void lockAccount(Long id) {
        DSLContext sql = databaseManager.getSqlDSL();
        boolean accountExists = sql.transactionResult(configuration ->
                DSL.using(configuration)
                        .update(ACCOUNT)
                        .set(ACCOUNT.LOCKED, true)
                        .where(ACCOUNT.ID.eq(id))
                        .execute() > 0);
        if (!accountExists) {
            throw new AccountNotFoundException(id);
        }
    }

    @Override
    public void unlockAccount(Long id) {
        DSLContext sql = databaseManager.getSqlDSL();
        boolean accountExists = sql.transactionResult(configuration ->
                DSL.using(configuration)
                        .update(ACCOUNT)
                        .set(ACCOUNT.LOCKED, false)
                        .where(ACCOUNT.ID.eq(id))
                        .execute() > 0);
        if (!accountExists) {
            throw new AccountNotFoundException(id);
        }
    }

    private Account convertFrom(AccountRecord accountRecord) {
        Account account = new Account();
        account.setId(accountRecord.getId());
        account.setLocked(accountRecord.getLocked());
        Money balance = new Money();
        balance.setAmount(accountRecord.getMoneyValue());
        balance.setCurrency(accountRecord.getMoneyCurrency());
        account.setBalance(balance);
        return account;
    }


}
