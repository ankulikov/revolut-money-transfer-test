package com.revolut.task.service.api;

public interface AccountLockManager {
    void createLock(Long accountId);

    void removeLock(Long accountId);


    void doInLock(Long accountId, Runnable action);

    void doInLock(Long accountId1, Long accountId2, Runnable action);
}
