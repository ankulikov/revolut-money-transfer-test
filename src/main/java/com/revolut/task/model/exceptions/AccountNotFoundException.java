package com.revolut.task.model.exceptions;

public class AccountNotFoundException extends RuntimeException {
    private Long accountId;

    public AccountNotFoundException(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }

    @Override
    public String getMessage() {
        return "Can't find account with ID="+accountId;
    }
}
