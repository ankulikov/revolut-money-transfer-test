package com.revolut.task.service.impl;

import com.google.inject.Singleton;
import com.revolut.task.service.api.AccountLockManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class AccountLockManagerImpl implements AccountLockManager {
    Map<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public void createLock(Long accountId) {
        locks.putIfAbsent(accountId, new ReentrantLock());
    }

    @Override
    public void removeLock(Long accountId) {
        locks.remove(accountId);
    }


    @Override
    public void doInLock(Long accountId, Runnable action) {
        createLock(accountId);
        ReentrantLock lock = locks.get(accountId);
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void doInLock(Long accountId1, Long accountId2, Runnable action) {
        createLock(accountId1);
        createLock(accountId2);
        ReentrantLock lock1 = locks.get(accountId1);
        ReentrantLock lock2 = locks.get(accountId2);
        boolean gotTwoLocks = false;
        do {
            if (lock1.tryLock()) {
                if (lock2.tryLock()) {
                    gotTwoLocks = true;
                } else {
                    lock1.unlock();
                }
            }
        } while (!gotTwoLocks);
        try {
            action.run();
        } finally {
            lock2.unlock();
            lock1.unlock();
        }
    }
}
