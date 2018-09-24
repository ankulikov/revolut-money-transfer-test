package com.revolut.task.di;

import com.google.inject.AbstractModule;
import com.revolut.task.service.api.AccountService;
import com.revolut.task.service.api.DatabaseManager;
import com.revolut.task.service.impl.AccountServiceImpl;
import com.revolut.task.service.impl.DatabaseManagerImpl;

public class AppInjectorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DatabaseManager.class).to(DatabaseManagerImpl.class);
        bind(AccountService.class).to(AccountServiceImpl.class);
    }
}
