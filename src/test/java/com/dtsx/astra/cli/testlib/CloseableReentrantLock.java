package com.dtsx.astra.cli.testlib;

import java.util.concurrent.locks.ReentrantLock;

public class CloseableReentrantLock extends ReentrantLock {
    public ActuallyCloseableReentrantLock use() {
        this.lock();
        return new ActuallyCloseableReentrantLock();
    }

    public class ActuallyCloseableReentrantLock implements AutoCloseable {
        @Override
        public void close() {
            CloseableReentrantLock.this.unlock();
        }
    }
}
