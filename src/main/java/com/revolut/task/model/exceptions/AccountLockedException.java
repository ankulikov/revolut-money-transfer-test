package com.revolut.task.model.exceptions;

public class AccountLockedException extends RuntimeException {
    private Long accountId;

    public AccountLockedException(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }

    @Override
    public String getMessage() {
        return "Account with ID=" + accountId + " is locked";
    }
}
