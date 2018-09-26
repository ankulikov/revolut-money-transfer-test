package com.revolut.task.service.impl;

import com.revolut.task.model.Account;
import com.revolut.task.model.Money;
import com.revolut.task.model.sql.tables.records.AccountRecord;
import com.revolut.task.service.api.AccountService;
import com.revolut.task.service.api.DatabaseManager;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;

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
    public Account getAccount(long id) {
        Optional<AccountRecord> possibleRecord = databaseManager.getSqlDSL()
                .selectFrom(ACCOUNT)
                .where(ACCOUNT.ID.eq(id))
                .fetchOptional();
        return possibleRecord.map(this::convertFrom).orElse(null);
    }

    @Override
    public boolean isLocked(Long id) {
        Record1<Boolean> locked = databaseManager.getSqlDSL()
                .select(ACCOUNT.LOCKED).from(ACCOUNT)
                .where(ACCOUNT.ID.eq(id))
                .fetchOne();
        if (locked == null) {
            throw new IllegalArgumentException("Can't find account with ID="+id);
        }
        return locked.get(ACCOUNT.LOCKED);
    }

    @Override
    public boolean removeAccount(long id) {
        return databaseManager.getSqlDSL()
                .deleteFrom(ACCOUNT)
                .where(ACCOUNT.ID.eq(id))
                .execute() > 0;
    }

    @Override
    public boolean lockAccount(long id) {
        DSLContext sql = databaseManager.getSqlDSL();
        return sql.transactionResult(configuration ->
                DSL.using(configuration)
                        .update(ACCOUNT)
                        .set(ACCOUNT.LOCKED, true)
                        .where(ACCOUNT.ID.eq(id))
                        .execute() > 0);
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
