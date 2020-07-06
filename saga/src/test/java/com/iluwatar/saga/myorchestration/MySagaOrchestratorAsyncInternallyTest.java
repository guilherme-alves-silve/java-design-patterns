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

package com.iluwatar.saga.myorchestration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.iluwatar.saga.myorchestration.application.*;
import com.iluwatar.saga.myorchestration.exception.InvalidConfigurationSagaOrchestratorException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * test to test orchestration logic
 */
public class MySagaOrchestratorAsyncInternallyTest {

	private static final int EXAMPLE_VALUE = 1;

	private final List<String> resultRecords = new ArrayList<>();

	@Test
	public void shouldProcessAndInTheEndRollbackBecauseOfFailures () {

		final var sagaOrchestrator = new MySagaOrchestrator<>(buildSaga(), buildServiceDiscovery());
		final var result = sagaOrchestrator.execute(EXAMPLE_VALUE);

		assertEquals("must rollback in the end because of failure", MySaga.Result.ROLLBACK, result);
		assertArrayEquals("Should process all and rollback all in the correct order, when the last failed",
				resultRecords.toArray(new String[] {}), new String[] {
						"Process Service1",
						"Process Service2",
						"Process Service3",
						"Process Service4",
						"Rollback Service4",
						"Rollback Service3",
						"Rollback Service2",
						"Rollback Service1",
				});
	}

	@Test(expected = InvalidConfigurationSagaOrchestratorException.class)
	public void shouldThrowExceptionIfChaptersAreConfigureIncorrectly () {
		
		new MySagaOrchestrator<>(buildSagaIncorrectly(), buildServiceDiscovery());
	}

	private MySaga buildSaga () {
		return MySaga.create()
				.chapter("Service1")
				.chapter("Service2")
				.chapter("Service3")
				.chapter("Service4 Fail in the end");
	}

	private MySaga buildSagaIncorrectly () {
		return MySaga.create()
				.chapter("Service1")
				.chapter("Service2")
				.chapter("ServiceNotExists3")
				.chapter("Service4 Fail in the end");
	}

	private MyServiceDiscovery<Integer> buildServiceDiscovery () {
		return MyServiceDiscovery.<Integer>create()
				.discover(new Service1())
				.discover(new Service2())
				.discover(new Service3())
				.discover(new Service4());
	}

	public class Service1 extends MyService<Integer> {

		@Override
		public String getName () {
			return this.getClass().getSimpleName();
		}

		@Override
		public MyChapterResult<Integer> process (final Integer value) {
			resultRecords.add("Process " + this.getClass().getSimpleName());
			return super.process(value);
		}

		@Override
		public MyChapterResult<Integer> rollback (final Integer value) {
			resultRecords.add("Rollback " + this.getClass().getSimpleName());
			return super.rollback(value);
		}
	}

	public class Service2 extends MyService<Integer> {

		@Override
		public MyChapterResult<Integer> process (final Integer value) {
			resultRecords.add("Process " + this.getClass().getSimpleName());
			return super.process(value);
		}

		@Override
		public MyChapterResult<Integer> rollback (final Integer value) {
			resultRecords.add("Rollback " + this.getClass().getSimpleName());
			return super.rollback(value);
		}
	}

	public class Service3 extends MyService<Integer> {

		@Override
		public MyChapterResult<Integer> process (final Integer value) {
			resultRecords.add("Process " + this.getClass().getSimpleName());
			return super.process(value);
		}

		@Override
		public MyChapterResult<Integer> rollback (final Integer value) {
			resultRecords.add("Rollback " + this.getClass().getSimpleName());
			return super.rollback(value);
		}
	}

	public class Service4 extends MyService<Integer> {

		@Override
		public String getName () {
			return this.getClass().getSimpleName() + " Fail in the end";
		}

		@Override
		public MyChapterResult<Integer> process (final Integer value) {
			resultRecords.add("Process " + this.getClass().getSimpleName());
			return MyChapterResult.failure(value);
		}

		@Override
		public MyChapterResult<Integer> rollback (final Integer value) {
			resultRecords.add("Rollback " + this.getClass().getSimpleName());
			return super.rollback(value);
		}
	}
}
