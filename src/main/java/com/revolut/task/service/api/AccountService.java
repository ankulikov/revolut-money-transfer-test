package com.revolut.task.service.api;

import com.revolut.task.model.Account;

public interface AccountService {
    Account createAccount();
    Account getAccount(String id);
    void removeAccount(String id);
    void lockAccount(String id);
}
