package com.revolut.task.model;

import lombok.Data;

@Data
public class Account {
    private String id;
    private boolean locked;
    private Money balance;
}
