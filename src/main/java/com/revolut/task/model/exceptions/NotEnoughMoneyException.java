package com.revolut.task.model.exceptions;

public class NotEnoughMoneyException extends RuntimeException {
    private Long accountId;
    private String operation;

    public NotEnoughMoneyException(Long accountId, String operation) {
        this.accountId = accountId;
        this.operation = operation;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String getMessage() {
        return "Account with ID="+accountId+" doesn't have enough money to complete " + operation;
    }
}
