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
        when:
        lockManager.doInLock("acc1") { println "lock acc1 must be created" }
        then:
        noExceptionThrown()
        lockManager.locks.containsKey("acc1")
    }

    def "remove and create lock"() {
        setup:
        lockManager.createLock("MY_LOCK")
        when:
        lockManager.removeLock("MY_LOCK")
        then:
        !lockManager.locks.containsKey("MY_LOCK")
        when:
        lockManager.createLock("MY_LOCK")
        then:
        lockManager.locks.containsKey("MY_LOCK")
    }

    def "remove nonexistent lock"() {
        when:
        lockManager.removeLock("SOME_LOCK_WHICH_DOESNT_EXIST")
        then:
        noExceptionThrown()
    }

    //stop condition for 'waiting'
    @Timeout(30)
    def "exception doesn't block account"() {
        setup:
        lockManager.createLock("A")
        when:
        taskExecutor.submit { lockManager.doInLock("A") { throw new RuntimeException("Some exception") } }.get()
        then:
        def ex = thrown(ExecutionException.class)
        ex.getCause().class == RuntimeException.class
        when:
        taskExecutor.submit { lockManager.doInLock("A") { println "just success action" } }.get()
        then:
        noExceptionThrown()
    }

    //stop condition for 'waiting'
    @Timeout(30)
    def "doInLock with permuted IDs doesn't cause deadlock"() {
        setup:
        lockManager.createLock("A")
        lockManager.createLock("B")
        def AtoBCounter = 0, BtoACounter = 0, tasks = []
        when:
        1000.times {
            tasks << { lockManager.doInLock("A", "B") { AtoBCounter++ } }
            tasks << { lockManager.doInLock("B", "A") { BtoACounter++ } }
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
        lockManager.createLock("A")
        lockManager.createLock("B")
        def ACounter = 0, BCounter = 0, AtoBCounter = 0, tasks = []
        when:
        1000.times {
            tasks << { lockManager.doInLock("A") { ACounter++ } }
            tasks << { lockManager.doInLock("A", "B") { AtoBCounter++ } }
            tasks << { lockManager.doInLock("B") { BCounter++ } }
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
        lockManager.createLock("A")
        lockManager.createLock("B")
        def ACounter = 0, BCounter = 0, AtoBCounter = 0, tasks = []
        when:
        1000.times { i ->
            tasks << {
                lockManager.doInLock("A") {
                    if (i % 2 == 0) ACounter++
                    else throw new RuntimeException("lock in A, exception")
                }
            }
            tasks << { lockManager.doInLock("A", "B") { AtoBCounter++ } }
            tasks << {
                lockManager.doInLock("B") {
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
        lockManager.createLock("A")
        lockManager.createLock("B")
        lockManager.createLock("C")
        def counter = 10, tasks = []
        when:
        1000.times {
            tasks << { lockManager.doInLock("A", "B") { counter++ } }
            tasks << { lockManager.doInLock("C", "B") { counter-- } }
        }
        taskExecutor.invokeAll(tasks).forEach({ it.get() })
        then:
        counter == 10
    }


    def cleanupSpec() {
        taskExecutor.shutdownNow()
    }
}
