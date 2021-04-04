package com.iluwatar.mythreadpool;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class MyApp {

    public static void main(String[] args) {

        final var threadPool = ThreadPool.newThreadPool(3);
        threadPool.start();

        final var maxCount = 100_000;
        final var numbers = Collections.synchronizedSet(new HashSet<Integer>());
        for (int i = 0; i < maxCount; i++) {
            var ii = i;
            threadPool.execute(() -> {
                if ((ii % 1_000) == 0) {
                    System.out.println("Thread " + Thread.currentThread().getName() + " has value " + ii);
                }

                numbers.add(ii);
            });
        }

        threadPool.awaitTermination(60, TimeUnit.SECONDS);

        System.out.println("Size: " + numbers.size());
        System.out.println("Has all numbers? " + (numbers.size() == maxCount));
    }
}
