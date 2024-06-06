package org.parsing4j.tokenizer.regex.structure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public abstract class Regex {

	public abstract List<Regex> getChildren();

	public abstract String getPrettyRepr();

	public List<Regex> unfold() {
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
		return new ArrayList<>(unfolded);
	}
	
	@Override
	public String toString() {
		return getPrettyRepr();
	}

}
