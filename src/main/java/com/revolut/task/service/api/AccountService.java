package com.revolut.task.service.api;

import com.revolut.task.model.Account;

public interface AccountService {
    Account createAccount();
    Account getAccount(long id);
    boolean removeAccount(long id);
    boolean lockAccount(long id);
}
