package com.revolut.task.service.impl

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class AccountLockManagerImplTest extends Specification {
    def lockManager = new AccountLockManagerImpl()
    @Shared
    def taskExecutor = Executors.newFixedThreadPool(4)

    def "implicit lock creation"() {
        setup:
        def ACC_ID = 1L
        when:
        lockManager.doInLock(ACC_ID) { println "lock for '1' must be created" }
        then:
        noExceptionThrown()
        lockManager.locks.containsKey(ACC_ID)
    }

    def "remove and create lock"() {
        setup:
        def ACC_ID = 100L
        lockManager.createLock(ACC_ID)
        when:
        lockManager.removeLock(ACC_ID)
        then:
        !lockManager.locks.containsKey(ACC_ID)
        when:
        lockManager.createLock(ACC_ID)
        then:
        lockManager.locks.containsKey(ACC_ID)
    }

    def "remove nonexistent lock"() {
        when:
        lockManager.removeLock(100500)
        then:
        noExceptionThrown()
    }

    //stop condition for 'waiting'
    @Timeout(30)
    def "exception doesn't block account"() {
        setup:
        def ACC_ID = 5L
        lockManager.createLock(ACC_ID)
        when:
        taskExecutor.submit { lockManager.doInLock(ACC_ID) { throw new RuntimeException("Some exception") } }.get()
        then:
        def ex = thrown(ExecutionException.class)
        ex.getCause().class == RuntimeException.class
        when:
        taskExecutor.submit { lockManager.doInLock(ACC_ID) { println "just success action" } }.get()
        then:
        noExceptionThrown()
    }

    //stop condition for 'waiting'
    @Timeout(30)
    def "doInLock with permuted IDs doesn't cause deadlock"() {
        setup:
        def A = 1L
        def B = 2L
        lockManager.createLock(A)
        lockManager.createLock(B)
        def AtoBCounter = 0, BtoACounter = 0, tasks = []
        when:
        1000.times {
            tasks << { lockManager.doInLock(A, B) { AtoBCounter++ } }
            tasks << { lockManager.doInLock(B, A) { BtoACounter++ } }
        }
        taskExecutor.invokeAll(tasks).forEach({ it.get() })
        then:
        AtoBCounter == 1000
        BtoACounter == 1000
    }

    //stop condition for 'waiting'
    @Timeout(30)
    def "doInLock for one and two accounts doesn't block each other"() {
        setup:
        def A = 1L
        def B = 2L
        lockManager.createLock(A)
        lockManager.createLock(B)
        def ACounter = 0, BCounter = 0, AtoBCounter = 0, tasks = []
        when:
        1000.times {
            tasks << { lockManager.doInLock(A) { ACounter++ } }
            tasks << { lockManager.doInLock(A, B) { AtoBCounter++ } }
            tasks << { lockManager.doInLock(B) { BCounter++ } }
        }
        taskExecutor.invokeAll(tasks).forEach({ it.get() })
        then:
        AtoBCounter == 1000
        ACounter == 1000
        BCounter == 1000
    }

    //stop condition for 'waiting'
    @Timeout(30)
    def "doInLock for one and two accounts doesn't block each other when exceptions"() {
        setup:
        def A = 1L
        def B = 2L
        lockManager.createLock(A)
        lockManager.createLock(B)
        def ACounter = 0, BCounter = 0, AtoBCounter = 0, tasks = []
        when:
        1000.times { i ->
            tasks << {
                lockManager.doInLock(A) {
                    if (i % 2 == 0) ACounter++
                    else throw new RuntimeException("lock in A, exception")
                }
            }
            tasks << { lockManager.doInLock(A, B) { AtoBCounter++ } }
            tasks << {
                lockManager.doInLock(B) {
                    if (i % 2 == 1) BCounter++
                    else throw new RuntimeException("lock in B, exception")
                }
            }
        }
        taskExecutor.invokeAll(tasks).forEach({
            try {
                it.get()
            } catch (Exception ignore) {}
        })
        then:
        AtoBCounter == 1000
        ACounter == 500 //even numbers from 0 to 999
        BCounter == 500 //odd numbers from 0 to 999
    }

    //stop condition for 'waiting'
    @Timeout(30)
    def "check concurrency for doInLock with two accounts"() {
        setup:
        def A = 1L
        def B = 2L
        def C = 3L
        lockManager.createLock(A)
        lockManager.createLock(B)
        lockManager.createLock(C)
        def counter = 10, tasks = []
        when:
        1000.times {
            tasks << { lockManager.doInLock(A, B) { counter++ } }
            tasks << { lockManager.doInLock(B, C) { counter-- } }
        }
        taskExecutor.invokeAll(tasks).forEach({ it.get() })
        then:
        counter == 10
    }


    def cleanupSpec() {
        taskExecutor.shutdownNow()
    }
}
