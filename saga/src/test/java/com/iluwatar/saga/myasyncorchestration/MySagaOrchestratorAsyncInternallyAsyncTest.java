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

package com.iluwatar.saga.myasyncorchestration;

import com.iluwatar.saga.myasyncorchestration.application.*;
import com.iluwatar.saga.myasyncorchestration.exception.InvalidConfigurationSagaOrchestratorAsyncException;
import com.iluwatar.saga.myorchestration.application.*;
import com.iluwatar.saga.myorchestration.exception.InvalidConfigurationSagaOrchestratorException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * test to test orchestration logic
 */
public class MySagaOrchestratorAsyncInternallyAsyncTest {

	private static final int EXAMPLE_VALUE = 1;

	private final List<String> resultRecords = new CopyOnWriteArrayList<>();

	@Test
	public void shouldProcessAndInTheEndRollbackBecauseOfFailures () {

		final var sagaOrchestrator = new MySagaOrchestratorAsync<>(buildSaga(), buildServiceDiscovery());
		final var result = sagaOrchestrator.execute(EXAMPLE_VALUE).join();

		assertEquals("must rollback in the end because of failure", MySagaAsync.Result.ROLLBACK, result);
		assertArrayEquals("Should process all and rollback all in the correct order, when the last failed",
				resultRecords.toArray(new String[] {}), new String[] {
						"Process Service1Async",
						"Process Service2Async",
						"Process Service3Async",
						"Process Service4Async",
						"Rollback Service4Async",
						"Rollback Service3Async",
						"Rollback Service2Async",
						"Rollback Service1Async",
				});
	}

	@Test(expected = InvalidConfigurationSagaOrchestratorAsyncException.class)
	public void shouldThrowExceptionIfChaptersAreConfigureIncorrectly () {
		
		new MySagaOrchestratorAsync<>(buildSagaIncorrectly(), buildServiceDiscovery());
	}

	private MySagaAsync buildSaga () {
		return MySagaAsync.create()
				.chapter("Service1Async")
				.chapter("Service2Async")
				.chapter("Service3Async")
				.chapter("Service4Async Fail in the end");
	}

	private MySagaAsync buildSagaIncorrectly () {
		return MySagaAsync.create()
				.chapter("Service1Async")
				.chapter("Service2Async")
				.chapter("ServiceNotExists3")
				.chapter("Service4Async Fail in the end");
	}

	private MyServiceDiscoveryAsync<Integer> buildServiceDiscovery () {
		return MyServiceDiscoveryAsync.<Integer>create()
				.discover(new Service1Async())
				.discover(new Service2Async())
				.discover(new Service3Async())
				.discover(new Service4Async());
	}

	public class Service1Async extends MyServiceAsync<Integer> {

		@Override
		public String getName () {
			return this.getClass().getSimpleName();
		}

		@Override
		public CompletableFuture<MyChapterResultAsync<Integer>> process (final Integer value) {
			resultRecords.add("Process " + this.getClass().getSimpleName());
			return super.process(value);
		}

		@Override
		public CompletableFuture<MyChapterResultAsync<Integer>> rollback (final Integer value) {
			resultRecords.add("Rollback " + this.getClass().getSimpleName());
			return super.rollback(value);
		}
	}

	public class Service2Async extends MyServiceAsync<Integer> {

		@Override
		public CompletableFuture<MyChapterResultAsync<Integer>> process (final Integer value) {
			resultRecords.add("Process " + this.getClass().getSimpleName());
			return super.process(value);
		}

		@Override
		public CompletableFuture<MyChapterResultAsync<Integer>> rollback (final Integer value) {
			resultRecords.add("Rollback " + this.getClass().getSimpleName());
			return super.rollback(value);
		}
	}

	public class Service3Async extends MyServiceAsync<Integer> {

		@Override
		public CompletableFuture<MyChapterResultAsync<Integer>> process (final Integer value) {
			resultRecords.add("Process " + this.getClass().getSimpleName());
			return super.process(value);
		}

		@Override
		public CompletableFuture<MyChapterResultAsync<Integer>> rollback (final Integer value) {
			resultRecords.add("Rollback " + this.getClass().getSimpleName());
			return super.rollback(value);
		}
	}

	public class Service4Async extends MyServiceAsync<Integer> {

		@Override
		public String getName () {
			return this.getClass().getSimpleName() + " Fail in the end";
		}

		@Override
		public CompletableFuture<MyChapterResultAsync<Integer>> process (final Integer value) {
			resultRecords.add("Process " + this.getClass().getSimpleName());
			LOGGER.info("The process of chapter '{}' started. But failed {}", getName(), value);
			return CompletableFuture.completedFuture(MyChapterResultAsync.failure(value));
		}

		@Override
		public CompletableFuture<MyChapterResultAsync<Integer>> rollback (final Integer value) {
			resultRecords.add("Rollback " + this.getClass().getSimpleName());
			return super.rollback(value);
		}
	}
}
