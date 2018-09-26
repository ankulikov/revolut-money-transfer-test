package com.revolut.task.service.api;

import com.revolut.task.model.Account;

public interface AccountService {
    Account createAccount();
    Account createAccount(String currency);
    Account getAccount(Long id);
    boolean isLocked(Long id);
    boolean removeAccount(long id);
    boolean lockAccount(long id);
}
