package com.iluwatar.mythreadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class ThreadPool implements Executor {

    private static final int SINGLE_THREADED = 1;
    private static final long DEFAULT_WAIT_MILLIS = 50;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPool.class);
    private volatile int numberOfTasks;
    private volatile int terminatedTasks;
    private final int maxThreadPoolSize;
    private volatile boolean running;
    private volatile int threadPoolSize;
    private final LinkedList<Runnable> tasks;
    private final Deque<WorkerThread> workerThreads;
    private Thread eventLoopThread;

    private ThreadPool(final int maxThreadPoolSize) {
        this.maxThreadPoolSize = maxThreadPoolSize;
        this.workerThreads = new ArrayDeque<>(maxThreadPoolSize);
        this.tasks = new LinkedList<>();
    }

    public static ThreadPool newThreadPool() {
        return newThreadPool(SINGLE_THREADED);
    }

    public static ThreadPool newThreadPool(final int maxThreadPoolSize) {
        final var threadPool = new ThreadPool(maxThreadPoolSize);
        threadPool.createEventLoopThread();
        return threadPool;
    }

    /**
     * Necessary to do in that way, so that the this reference don't escape
     * and the new Thread access the ThreadPool in an inconsistent way.
     * References:
     *   https://stackoverflow.com/questions/3705425/java-reference-escape/27139005
     *   https://hashnode.com/post/introduction-to-anatomy-of-escaping-references-using-java-ckdzud9j3000nnas1dbe86d6g
     */
    private void createEventLoopThread() {
        this.eventLoopThread = new Thread(() -> {
            OUTER_LOOP:
            while (running) {

                final Runnable command;
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait(DEFAULT_WAIT_MILLIS);
                        } catch (InterruptedException ex) {
                            configureTermination("ThreadPool interrupted on commands: {}", ex);
                            break OUTER_LOOP;
                        }
                    }

                    command = tasks.pollFirst();
                }

                final WorkerThread workerThread;
                synchronized (workerThreads) {
                    while (workerThreads.isEmpty()) {
                        try {
                            workerThreads.wait(DEFAULT_WAIT_MILLIS);
                        } catch (InterruptedException ex) {
                            configureTermination("ThreadPool interrupted on workerThreads: {}", ex);
                            break OUTER_LOOP;
                        }
                    }

                    workerThread = workerThreads.pollFirst();
                }

                workerThread.setTask(command);
                workerThread.wakeUp();
            }
        });
    }

    private void configureTermination(String message, InterruptedException ex) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(message, ex.getMessage());
        }
        Thread.currentThread().interrupt();
        running = false;
    }

    public void preStartAllThreads() {

        start();

        synchronized (workerThreads) {
            for (int i = 0; i < maxThreadPoolSize; ++i) {
                workerThreads.add(createWorkerThread(++threadPoolSize));
            }
        }
    }

    private WorkerThread createWorkerThread(final int threadWorkerNumber) {
        final var worker = new WorkerThread(threadWorkerNumber, workerThreads);
        worker.start();
        return worker;
    }

    public int getMaxThreadPoolSize() {
        return maxThreadPoolSize;
    }

    public boolean isRunning() {
        return running;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    private void createNewWorkerThread() {

        if ((threadPoolSize >= maxThreadPoolSize) || !running) {
            return;
        }

        synchronized (workerThreads) {

            if ((threadPoolSize >= maxThreadPoolSize) || !running) {
                return;
            }

            workerThreads.add(createWorkerThread(++threadPoolSize));
            workerThreads.notify();
        }
    }

    /**
     *
     * @param task task to be executed
     * @throws NullPointerException if task is null
     */
    @Override
    public void execute(final Runnable task) {
        Objects.requireNonNull(task, "commands cannot be null!");

        createNewWorkerThread();

        if (!running) {
            throw new IllegalArgumentException("ThreadPool already shutdown!");
        }

        synchronized (tasks) {
            ++numberOfTasks;
            tasks.add(task);
            tasks.notify();
        }
    }

    public void start() {

        if (running) {
            throw new IllegalArgumentException("Already started!");
        }

        synchronized (this) {
            if (running) {
                return;
            }

            eventLoopThread.start();
            running = true;
        }
    }

    public void shutdownNow() {
        shutdown();
        final var workerThreads = getWorkerThreads();
        workerThreads.forEach(WorkerThread::terminate);
        eventLoopThread.interrupt();
    }

    public void shutdown() {
        this.running = false;
    }

    public int getNumberOfTasks() {
        return numberOfTasks;
    }

    public void awaitTermination(long timeout, TimeUnit unit) {
        final long min = System.currentTimeMillis();
        final long max = min + unit.toMillis(timeout);
        while (running || getWorkerThreads().stream().anyMatch(Thread::isAlive)) {
            Thread.onSpinWait();
            final long now = System.currentTimeMillis();
            if (now > max || finishedWork()) {
                break;
            }
        }

        shutdownNow();
    }

    private LinkedList<WorkerThread> getWorkerThreads() {
        final LinkedList<WorkerThread> workerThreadsToTerminate;
        synchronized (workerThreads) {
            workerThreadsToTerminate = new LinkedList<>(workerThreads);
        }

        return workerThreadsToTerminate;
    }

    private boolean finishedWork() {
        return numberOfTasks == terminatedTasks;
    }

    private class WorkerThread extends Thread {

        private volatile boolean terminated;
        private volatile Runnable task;
        private final Deque<WorkerThread> workerThreads;

        private WorkerThread(final int number, final Deque<WorkerThread> workerThreads) {
            super("worker-thread-" + number);
            this.terminated = false;
            this.workerThreads = workerThreads;
        }

        public void terminate() {
            this.terminated = true;
            this.interrupt();
        }

        public void setTask(final Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {

            while (!terminated) {

                if (null == task) {
                    synchronized (this) {
                        try {
                            this.wait(DEFAULT_WAIT_MILLIS);
                        } catch (InterruptedException ex) {
                            interrupt();
                        }
                    }

                    continue;
                }

                try {
                    task.run();
                    task = null;
                    synchronized (workerThreads) {
                        terminatedTasks++;
                        workerThreads.add(this); //finished it's work so add again to thread-pool list
                        workerThreads.notify();  //notify the ThreadPool that his ready to work again
                    }
                } catch (Throwable th) {
                    getUncaughtExceptionHandler().uncaughtException(this, th);
                }
            }
        }

        public void wakeUp() {
            synchronized (this) {
                this.notify();
            }
        }
    }
}
