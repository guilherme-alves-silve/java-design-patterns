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

package com.iluwatar.saga.myorchestration.application;

import static com.iluwatar.saga.myorchestration.application.MySaga.*;

import com.iluwatar.saga.myorchestration.exception.InvalidConfigurationSagaOrchestratorException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author guilherme
 * @version : $<br/>
 * : $
 * @since 30/06/2020 20:33
 */
public class MySagaOrchestrator<K> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MySagaOrchestrator.class);

	private final List<MyOrchestrationChapter<K>> servicesChapters;

	private final MySaga saga;

	public MySagaOrchestrator (final MySaga saga, final MyServiceDiscovery<K> serviceDiscovery) {
		Objects.requireNonNull(serviceDiscovery, "serviceDiscovery cannot be null!");
		this.saga = Objects.requireNonNull(saga, "saga cannot be null!");
		this.servicesChapters = getOrchestrationChapters(saga, serviceDiscovery);
	}

	public Result execute (final K value) {

		LOGGER.info("The saga is about to start!");

		final var state = new CurrentState();

		var result = Result.FINISHED;
		K tempVal = value;

		while (true) {
			
			final var service = servicesChapters.get(state.current());

			if (state.isForward()) {
				final var serviceResult = service.process(tempVal);
				if (serviceResult.isSuccess()) {
					tempVal = serviceResult.getValue();
					state.forward();
				} else {
					state.mustRollback();
					result = Result.ROLLBACK;
				}
			} else {
				final var serviceResult = service.rollback(tempVal);
				if (serviceResult.isSuccess()) {
					tempVal = serviceResult.getValue();
				} else {
					result = Result.CRASHED;
				}

				state.back();
			}

			if (!saga.isIndexInRange(state.current())) {
				return result;
			}

		}
	}

	private List<MyOrchestrationChapter<K>> getOrchestrationChapters(
			final MySaga saga,
			final MyServiceDiscovery<K> serviceDiscovery
	) {
		return StreamSupport.stream(saga.spliterator(), false)
					.map(chapter -> serviceDiscovery.find(chapter.getName()))
					.map(optService -> optService.orElseThrow(InvalidConfigurationSagaOrchestratorException::new))
					.collect(Collectors.toList());
	}

	private static final class CurrentState {

		private int currentNumber;

		private boolean isForward;

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
