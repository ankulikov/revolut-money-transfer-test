package com.revolut.task.model;

import lombok.Data;

@Data
public class Account {
    private Long id;
    private boolean locked;
    private Money balance;
}
