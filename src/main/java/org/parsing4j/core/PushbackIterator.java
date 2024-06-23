package org.parsing4j.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/*
 * @author Florent Guille
 * */
public class PushbackIterator<T> implements Iterator<T> {

	private Iterator<T> target;
	private Deque<T> stack;

	public PushbackIterator(Iterator<T> target) {
		this.target = target;
		this.stack = new ArrayDeque<>();
	}

	public void pushback(T item) {
		stack.push(item);
	}

	@Override
	public boolean hasNext() {
		return !stack.isEmpty() || target.hasNext();
	}

	@Override
	public T next() {
		if (!stack.isEmpty()) {
			return stack.pop();
		}
		return target.next();
	}

}