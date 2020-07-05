package com.iluwatar.saga.myasyncorchestration.exception;

public class MyChapterResultAsyncFailedExcption<K> {

    private final K value;

    public MyChapterResultAsyncFailedExcption(K value) {
        this.value = value;
    }
}
