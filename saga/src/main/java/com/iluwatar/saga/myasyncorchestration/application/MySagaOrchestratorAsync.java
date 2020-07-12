/*
 * The MIT License
 * Copyright © 2014-2019 Ilkka Seppälä
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.iluwatar.saga.myasyncorchestration.application;

import com.iluwatar.saga.myasyncorchestration.exception.InvalidConfigurationSagaOrchestratorAsyncException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.iluwatar.saga.myasyncorchestration.application.MySagaAsync.Result;

/**
 * @author guilherme
 * @version : $<br/>
 * : $
 * @since 30/06/2020 20:33
 */
public class MySagaOrchestratorAsync<K> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MySagaOrchestratorAsync.class);

	private final List<MyOrchestrationChapterAsync<K>> servicesChapters;

	private final MySagaAsync saga;

	private final ExecutorService executor;

	public MySagaOrchestratorAsync(final MySagaAsync saga, final MyServiceDiscoveryAsync<K> serviceDiscovery) {
		Objects.requireNonNull(serviceDiscovery, "serviceDiscovery cannot be null!");
		this.saga = Objects.requireNonNull(saga, "saga cannot be null!");
		this.servicesChapters = getOrchestrationChapters(saga, serviceDiscovery);
		final var threadFactory = new ThreadFactory() {

			private final AtomicInteger counter = new AtomicInteger();

			@Override
			public Thread newThread(Runnable runnable) {
				return new Thread(runnable, "saga-orchestrator-async-" + counter.incrementAndGet());
			}
		};
		this.executor = Executors.newSingleThreadExecutor(threadFactory);
	}

	public CompletableFuture<Result> execute (final K value) {

		LOGGER.info("The saga is about to start!");

		final var state = new CurrentState();

		return sagaAsync(value, state, Result.FINISHED)
				.thenApply(processed -> processed.sagaResult);
	}

	public void shutdown(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(timeout, timeUnit);
	}

	private CompletableFuture<SagaResult<K>> asyncProcess(final MyChapterResultAsync<K> asyncValue, final CurrentState state, final Result sagaLastResult) {

		final Result sagaUpdatedResult;
		if (!asyncValue.isSuccess()) {
			state.mustRollback();

			if (Result.ROLLBACK == sagaLastResult) {
				sagaUpdatedResult = Result.CRASHED;
			} else {
				state.forward();
				sagaUpdatedResult = Result.ROLLBACK;
			}
		} else {
			sagaUpdatedResult = sagaLastResult;
		}

		if (state.isForward()) {
			state.forward();
		} else {
			state.back();
		}

		return sagaAsync(asyncValue.getValue(), state, sagaUpdatedResult);
	}

	private CompletableFuture<SagaResult<K>> sagaAsync(K value, CurrentState state, final Result sagaLastResult) {

		if (!saga.isIndexInRange(state.current())) {
			final var chapterResult = MyChapterResultAsync.success(value);
			return SagaResult.withSagaAndChapterResultAsync(sagaLastResult, chapterResult);
		}

		return CompletableFuture.supplyAsync(() -> servicesChapters.get(state.current()), executor)
				.thenCompose(service -> {
					if (state.isForward()) {
						return service.process(value)
								.thenCompose(asyncValue -> asyncProcess(asyncValue, state, sagaLastResult));
					}

					return service.rollback(value)
							.thenCompose(asyncValue -> asyncProcess(asyncValue, state, sagaLastResult));
				});
	}

	private List<MyOrchestrationChapterAsync<K>> getOrchestrationChapters(
			final MySagaAsync saga,
			final MyServiceDiscoveryAsync<K> serviceDiscovery
	) {
		return StreamSupport.stream(saga.spliterator(), false)
				.map(chapter -> serviceDiscovery.find(chapter.getName()))
				.map(optService -> optService.orElseThrow(InvalidConfigurationSagaOrchestratorAsyncException::new))
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));
	}

	private static final class SagaResult<K> {

		final Result sagaResult;
		final MyChapterResultAsync<K> chapterResult;

		private SagaResult(Result sagaResult, MyChapterResultAsync<K> chapterResult) {
			this.sagaResult = sagaResult;
			this.chapterResult = chapterResult;
		}

		public static <K> SagaResult<K> withSagaAndChapterResult(Result sagaResult, MyChapterResultAsync<K> chapterResult) {
			return new SagaResult<>(sagaResult, chapterResult);
		}

		public static <K> CompletableFuture<SagaResult<K>> withSagaAndChapterResultAsync(Result sagaResult, MyChapterResultAsync<K> chapterResult) {
			return CompletableFuture.completedFuture(new SagaResult<>(sagaResult, chapterResult));
		}
	}

	private static final class CurrentState {

		private volatile int currentNumber;

		private volatile boolean isForward;

		CurrentState() {
			this.currentNumber = 0;
			this.isForward = true;
		}

		void mustRollback () {
			isForward = false;
		}

		boolean isForward() {
			return isForward;
		}

		int current() {
			return currentNumber;
		}

		int forward() {
			return ++currentNumber;
		}

		int back() {
			return --currentNumber;
		}
	}
}
