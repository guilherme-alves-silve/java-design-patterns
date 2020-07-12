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

import com.iluwatar.saga.myasyncorchestration.application.MySagaAsync;
import com.iluwatar.saga.myasyncorchestration.application.MySagaOrchestratorAsync;
import com.iluwatar.saga.myasyncorchestration.application.MyServiceDiscoveryAsync;
import com.iluwatar.saga.myasyncorchestration.service.MyFlyBookingServiceAsync;
import com.iluwatar.saga.myasyncorchestration.service.MyHotelBookingServiceAsync;
import com.iluwatar.saga.myasyncorchestration.service.MyOrderServiceAsync;
import com.iluwatar.saga.myasyncorchestration.service.MyWithdrawMoneyServiceAsync;

import java.util.concurrent.TimeUnit;

/**
 * @author guilherme
 * @version : $<br/>
 * : $
 * @since 30/06/2020 20:25
 */
public class MySagaApplicationAsync {

	public static void main (String[] args) throws InterruptedException {
		final var sagaOrchestrator = new MySagaOrchestratorAsync<>(newSaga(), serviceDiscovery());
		final var rollbakOrder = sagaOrchestrator.execute("flux_must_rollback_order").join();
		System.out.println("rollbakOrder: " + rollbakOrder);
		sagaOrchestrator.shutdown(1L, TimeUnit.SECONDS);
	}

	private static MyServiceDiscoveryAsync<String> serviceDiscovery() {
		return MyServiceDiscoveryAsync.<String>create()
				.discover(new MyOrderServiceAsync())
				.discover(new MyFlyBookingServiceAsync())
				.discover(new MyHotelBookingServiceAsync())
				.discover(new MyWithdrawMoneyServiceAsync());
	}

	private static MySagaAsync newSaga() {
		return MySagaAsync.create()
				.chapter("init an order")
				.chapter("booking a Fly")
				.chapter("booking a Hotel")
				.chapter("withdrawing Money");
	}
}
