package com.iluwatar.mythreadpool;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadPoolTest {

    @Test
    void shouldCreateSingleThreadedPoolCorrectly() {

        final var threadPool = ThreadPool.newThreadPool();
        assertAll(
                () -> assertEquals(1, threadPool.getMaxThreadPoolSize(), "getMaxThreadPoolSize"),
                () -> assertFalse(threadPool.isRunning(), "isRunning"),
                () -> assertEquals(0, threadPool.getNumberOfTasks(), "getNumberOfTasks"),
                () -> assertEquals(0, threadPool.getThreadPoolSize(), "getThreadPoolSize")
        );
    }

    @Test
    void shouldCreateThreadPoolCorrectly() {

        final var threadPool = ThreadPool.newThreadPool(3);
        assertAll(
                () -> assertEquals(3, threadPool.getMaxThreadPoolSize(), "getMaxThreadPoolSize"),
                () -> assertFalse(threadPool.isRunning(), "isRunning"),
                () -> assertEquals(0, threadPool.getNumberOfTasks(), "getNumberOfTasks"),
                () -> assertEquals(0, threadPool.getThreadPoolSize(), "getThreadPoolSize")
        );
    }

    @Test
    void shouldPrestartAllThreadCreateMaximumNumberOfWorkers() {

        final int threadPoolSize = 4;
        final var threadPool = ThreadPool.newThreadPool(threadPoolSize);
        threadPool.preStartAllThreads();
        assertAll(
                () -> assertEquals(threadPoolSize, threadPool.getMaxThreadPoolSize(), "getMaxThreadPoolSize"),
                () -> assertTrue(threadPool.isRunning(), "isRunning"),
                () -> assertEquals(0, threadPool.getNumberOfTasks(), "getNumberOfTasks"),
                () -> assertEquals(threadPoolSize, threadPool.getThreadPoolSize(), "getThreadPoolSize")
        );
    }

        @Test
    void shouldExecuteAllTasks() {

        final int threadPoolSize = 4;
        final var threadPool = ThreadPool.newThreadPool(threadPoolSize);
        threadPool.start();

        assertAll(
                () -> assertEquals(4, threadPool.getMaxThreadPoolSize(), "getMaxThreadPoolSize"),
                () -> assertTrue(threadPool.isRunning(), "isRunning"),
                () -> assertEquals(0, threadPool.getNumberOfTasks(), "getNumberOfTasks"),
                () -> assertEquals(0, threadPool.getThreadPoolSize(), "getThreadPoolSize")
        );

        final var maxCount = 10_000;
        final var numbers = Collections.synchronizedSet(new HashSet<Integer>());
        for (int i = 0; i < maxCount; i++) {
            var ii = i;
            threadPool.execute(() -> {
                numbers.add(ii);
            });
        }

        assertEquals(threadPoolSize, threadPool.getThreadPoolSize());

        threadPool.awaitTermination(60, TimeUnit.SECONDS);

        assertAll(
                () -> assertFalse(threadPool.isRunning(), "isRunning"),
                () -> assertEquals(maxCount, numbers.size(), "maxCount == numbers.size"),
                () -> assertEquals(maxCount, threadPool.getNumberOfTasks(), "getNumberOfTasks")
        );
    }
}
