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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author guilherme
 * @version : $<br/>
 * : $
 * @since 30/06/2020 20:34
 */
public class MySaga implements Iterable<MySaga.MyChapter> {

	private final List<MyChapter> chapters;

	private MySaga () {
		this.chapters = new ArrayList<>();
	}

	public static MySaga create () {
		return new MySaga();
	}

	public MySaga chapter (final String chapterName) {
		chapters.add(new MyChapter(chapterName));
		return this;
	}

	public MyChapter getChapter (final int index) {
		return chapters.get(index);
	}

	public boolean isIndexInRange (final int index) {
		return index >= 0 && (index < chapters.size());
	}

	public int totalChapters () {
		return chapters.size();
	}

	@Override
	public Iterator<MyChapter> iterator () {
		return new ArrayList<>(chapters).iterator();
	}

	public enum Result {
		FINISHED, ROLLBACK, CRASHED
	}

	public class MyChapter {

		private final String name;

		public MyChapter (final String name) {
			this.name = Objects.requireNonNull(name, "name cannot be null!");
		}

		public String getName () {
			return name;
		}
	}
}
