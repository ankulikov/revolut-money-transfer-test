package com.revolut.task.di;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class InjectorProvider {
    private static Injector injector;

    static {
        injector = Guice.createInjector(new AppInjectorModule());
    }

    public static Injector provide() {
        return injector;
    }
}
