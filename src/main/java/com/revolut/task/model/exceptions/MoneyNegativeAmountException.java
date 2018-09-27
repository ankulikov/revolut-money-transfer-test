package com.revolut.task.model.exceptions;

public class MoneyNegativeAmountException extends RuntimeException {
    private String operation;

    public MoneyNegativeAmountException(String operation) {
        this.operation = operation;
    }

    @Override
    public String getMessage() {
        return "Can't " + operation + " negative amount of money";
    }
}
