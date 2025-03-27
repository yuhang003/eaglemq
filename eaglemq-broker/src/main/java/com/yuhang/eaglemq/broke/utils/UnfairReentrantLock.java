package com.yuhang.eaglemq.broke.utils;

import java.util.concurrent.locks.ReentrantLock;

public class UnfairReentrantLock implements PutMessageLock {

    private final ReentrantLock reentrantLock = new ReentrantLock();

    @Override
    public void lock() {
        reentrantLock.lock();
    }

    @Override
    public void unlock() {
        reentrantLock.unlock();
    }
}
