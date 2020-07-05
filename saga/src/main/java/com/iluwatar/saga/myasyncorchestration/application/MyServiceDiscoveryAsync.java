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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author guilherme
 * @version : $<br/>
 * : $
 * @since 30/06/2020 20:44
 */
public class MyServiceDiscovery<K> {

	private final Map<String, MyOrchestrationChapterAsync<K>> orchestrationsMap;

	private MyServiceDiscovery () {
		this.orchestrationsMap = new HashMap<>();
	}

	public Optional<MyOrchestrationChapterAsync<K>> find(final String chapterName) {
		Objects.requireNonNull(chapterName, "chapterName cannot be null");
		return Optional.ofNullable(orchestrationsMap.get(chapterName));
	}

	public MyServiceDiscovery<K> discover(final MyOrchestrationChapterAsync<K> orchestrationChapter) {
		Objects.requireNonNull(orchestrationChapter, "orchestrationChapter cannot be null");
		orchestrationsMap.put(orchestrationChapter.getName(), orchestrationChapter);
		return this;
	}

	public static <K> MyServiceDiscovery<K> create() {
		return new MyServiceDiscovery<>();
	}
}
