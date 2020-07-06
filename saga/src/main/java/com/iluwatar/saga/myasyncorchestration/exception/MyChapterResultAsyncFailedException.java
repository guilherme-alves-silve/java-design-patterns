package com.iluwatar.saga.myasyncorchestration.exception;

import com.iluwatar.saga.myasyncorchestration.application.MyChapterResultAsync;

public class MyChapterResultAsyncFailedException extends RuntimeException {

    private final Object value;
    private final MyChapterResultAsync.State state;

    public MyChapterResultAsyncFailedException(final Object value, final MyChapterResultAsync.State state) {
        this.value = value;
        this.state = state;
    }
}
