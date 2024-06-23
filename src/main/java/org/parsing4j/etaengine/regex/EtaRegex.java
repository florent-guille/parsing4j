package org.parsing4j.etaengine.regex;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/*
 * @author Florent Guille
 * */
public abstract class EtaRegex {

	public abstract List<EtaRegex> getChildren();

	public abstract String getRepr();

	public abstract boolean simpleEquals(EtaRegex regex);

	@Override
	public boolean equals(Object o) {
		if (o instanceof EtaRegex regex) {
			Deque<EtaRegex> left = new ArrayDeque<>();
			Deque<EtaRegex> right = new ArrayDeque<>();
			left.push(this);
			right.push(regex);

			while (!left.isEmpty()) {
				EtaRegex leftRegex = left.pop();
				EtaRegex rightRegex = right.pop();

				if (!leftRegex.simpleEquals(rightRegex)) {
					return false;
				}

				List<EtaRegex> leftChildren = leftRegex.getChildren();
				List<EtaRegex> rightChildren = rightRegex.getChildren();
				if (leftChildren.size() != rightChildren.size()) {
					return false;
				}

				for (EtaRegex leftChild : leftChildren) {
					left.push(leftChild);
				}

				for (EtaRegex rightChild : rightChildren) {
					right.push(rightChild);
				}
			}
			return true;
		}
		return false;
	}
}