package com.revolut.task.di;

import com.google.inject.AbstractModule;
import com.revolut.task.service.api.*;
import com.revolut.task.service.impl.*;

public class AppInjectorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DatabaseManager.class).to(DatabaseManagerImpl.class);
        bind(AccountLockManager.class).to(AccountLockManagerImpl.class);
        bind(ExchangeService.class).to(ExchangeServiceImpl.class);
        bind(AccountService.class).to(AccountServiceImpl.class);
        bind(MoneyService.class).to(MoneyServiceImpl.class);

    }
}
