package com.revolut.task.service.impl;

import com.revolut.task.model.Account;
import com.revolut.task.model.Money;
import com.revolut.task.model.sql.tables.records.AccountRecord;
import com.revolut.task.service.api.AccountService;
import com.revolut.task.service.api.DatabaseManager;

import javax.inject.Inject;
import java.util.Optional;

import static com.revolut.task.model.sql.Tables.ACCOUNT;

public class AccountServiceImpl implements AccountService {
    private DatabaseManager databaseManager;

    @Inject
    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
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
    public Account getAccount(String id) {
        Optional<AccountRecord> possibleRecord = databaseManager.getSqlDSL()
                .selectFrom(ACCOUNT)
                .where(ACCOUNT.ID.eq(Long.valueOf(id)))
                .fetchOptional();
        return possibleRecord.map(this::convertFrom).orElse(null);
    }

    @Override
    public void removeAccount(String id) {
        databaseManager.getSqlDSL()
                .deleteFrom(ACCOUNT)
                .where(ACCOUNT.ID.eq(Long.valueOf(id)))
                .execute();
    }

    @Override
    public void lockAccount(String id) {
        databaseManager.getSqlDSL()
                .update(ACCOUNT)
                .set(ACCOUNT.LOCKED, true)
                .where(ACCOUNT.ID.eq(Long.valueOf(id)))
                .execute();
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
