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

package com.iluwatar.saga.myasyncorchestration.service;

import com.iluwatar.saga.myasyncorchestration.application.MyChapterResultAsync;
import com.iluwatar.saga.myasyncorchestration.application.MyServiceAsync;

import java.util.concurrent.CompletableFuture;

/**
 * @author guilherme
 * @version : $<br/>
 * : $
 * @since 03/07/2020 15:57
 */
public class MyWithdrawMoneyServiceAsync extends MyServiceAsync<String> {

    @Override
    public String getName() {
        return "withdrawing Money";
    }

    @Override
    public CompletableFuture<MyChapterResultAsync<String>> process(String value) {

        if (value.equals("flux_must_rollback_order") || value.equals("flux_must_crash_order")) {
            LOGGER.warn("The flux '{}' entered. The chapter '{}' failed to process value '{}'", value, getName(), value);
            return CompletableFuture.completedFuture(MyChapterResultAsync.failure(value));
        }

        return super.process(value);
    }
}
