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

import com.iluwatar.saga.myorchestration.application.MySaga;
import com.iluwatar.saga.myorchestration.application.MySagaOrchestrator;
import com.iluwatar.saga.myorchestration.application.MyServiceDiscovery;
import com.iluwatar.saga.myorchestration.service.MyFlyBookingService;
import com.iluwatar.saga.myorchestration.service.MyHotelBookingService;
import com.iluwatar.saga.myorchestration.service.MyOrderService;
import com.iluwatar.saga.myorchestration.service.MyWithdrawMoneyService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author guilherme
 * @version : $<br/>
 * : $
 * @since 03/07/2020 16:00
 */
public class MySagaOrchestratorTest {

	@Test
	public void shouldRollbackSuccessfully() {

		final var sagaOrchestrator = new MySagaOrchestrator<>(newSaga(), serviceDiscovery());
		final var rollbakOrder = sagaOrchestrator.execute("flux_must_rollback_order");
		assertEquals(MySaga.Result.ROLLBACK, rollbakOrder);
	}

	@Test
	public void shouldTryToRollbackEvenIfOneServiceCrashed() {

		final var sagaOrchestrator = new MySagaOrchestrator<>(newSaga(), serviceDiscovery());
		final var crashedOrder = sagaOrchestrator.execute("flux_must_crash_order");
		assertEquals(MySaga.Result.CRASHED, crashedOrder);
	}

	private MyServiceDiscovery<String> serviceDiscovery() {
		return MyServiceDiscovery.<String>create()
				.discover(new MyOrderService())
				.discover(new MyFlyBookingService())
				.discover(new MyHotelBookingService())
				.discover(new MyWithdrawMoneyService());
	}

	private MySaga newSaga() {
		return MySaga.create()
				.chapter("init an order")
				.chapter("booking a Fly")
				.chapter("booking a Hotel")
				.chapter("withdrawing Money");
	}
}
