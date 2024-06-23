package org.parsing4j.tokenizer.regex;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/*
 * @author Florent Guille
 * */
public abstract class Regex {

	public abstract List<Regex> getChildren();

	public abstract String getRepr();

	public Deque<Regex> unfold() {
		Deque<Regex> input = new ArrayDeque<>();
		Deque<Regex> unfolded = new ArrayDeque<>();
		input.push(this);

		while (!input.isEmpty()) {
			Regex current = input.pop();
			unfolded.push(current);

			for (Regex child : current.getChildren()) {
				input.push(child);
			}
		}
		return unfolded;
	}

	@Override
	public String toString() {
		return getRepr();
	}

}