package com.revolut.task.service.api;

public interface AccountLockManager {
    void createLock(String accountId);

    void removeLock(String accountId);

    void acquireLock(String accountId);

    void releaseLock(String accountId);

    void doInLock(String accountId, Runnable action);

    void doInLock(String accountId1, String accountId2, Runnable action);
}
