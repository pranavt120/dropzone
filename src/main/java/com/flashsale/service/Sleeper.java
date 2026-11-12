package com.flashsale.service;

@FunctionalInterface
public interface Sleeper {
    void sleep(int seconds) throws InterruptedException;
}
