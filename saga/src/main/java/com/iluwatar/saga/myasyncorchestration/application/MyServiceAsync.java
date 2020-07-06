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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author guilherme
 * @version : $<br/>
 * : $
 * @since 30/06/2020 20:14
 */
public abstract class MyServiceAsync<K> implements MyOrchestrationChapterAsync<K> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(MyServiceAsync.class);

	@Override
	public String getName () {
		return this.getClass().getSimpleName();
	}

	@Override
	public CompletableFuture<MyChapterResultAsync<K>> process (final K value) {

		LOGGER.info("The process of chapter '{}' started. Processed value {} successfully", getName(), value);

		return CompletableFuture.completedFuture(MyChapterResultAsync.success(value));
	}

	@Override
	public CompletableFuture<MyChapterResultAsync<K>> rollback (final K value) {

		LOGGER.warn("The rollback of chapter '{}' started. Rollbacked value {} successfully", getName(), value);

		return CompletableFuture.completedFuture(MyChapterResultAsync.success(value));
	}
}
