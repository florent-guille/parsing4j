package org.parsing4j.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Utils {

	public static <T> String treeRepr(T root, Function<? super T, ? extends String> nameExtractor,
			Function<? super T, ? extends List<T>> childrenExtractor, String rootHeadPrefix, String rootBodyPrefix) {
		StringBuilder builder = new StringBuilder();

		Deque<T> objectStack = new ArrayDeque<>();
		Deque<String> headPrefixStack = new ArrayDeque<>();
		Deque<String> bodyPrefixStack = new ArrayDeque<>();

		objectStack.push(root);
		headPrefixStack.push(rootHeadPrefix);
		bodyPrefixStack.push(rootBodyPrefix);

		while (!objectStack.isEmpty()) {
			T current = objectStack.pop();
			String headPrefix = headPrefixStack.pop();
			String bodyPrefix = bodyPrefixStack.pop();

			builder.append(headPrefix);
			builder.append(nameExtractor.apply(current));

			List<T> children = childrenExtractor.apply(current);

			for (int i = children.size() - 1; i >= 0; i--) {
				objectStack.push(children.get(i));
				if (i == children.size() - 1) {
					headPrefixStack.push(bodyPrefix + "└─");
					bodyPrefixStack.push(bodyPrefix + "  ");
				} else {
					headPrefixStack.push(bodyPrefix + "├─");
					bodyPrefixStack.push(bodyPrefix + "│ ");
				}
			}

			if (!objectStack.isEmpty()) {
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	public static <T> String treeRepr(T current, Function<? super T, ? extends String> nameExtractor,
			Function<? super T, List<T>> childrenExtractor) {
		return treeRepr(current, nameExtractor, childrenExtractor, "", "");
	}

	public static int getDigitValue(char c, int base) {
		if (c >= 0x30 && c <= 0x39) {
			return c - 0x30;
		}

		if (c >= 0x41 && c <= 0x5A) {
			return c - 0x41 + 10;
		}

		if (c >= 0x61 && c <= 0x7D) {
			return c - 0x61 + 10;
		}
		return -1;
	}

	public static class Pair<A, B> {
		public A left;
		public B right;

		public Pair(A left, B right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public int hashCode() {
			return Objects.hash(left, right);
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Pair pair && Objects.equals(this.left, pair.left)
					&& Objects.equals(this.right, pair.right);
		}

		@Override
		public String toString() {
			return "(" + left + "," + right + ")";
		}

	}

	@FunctionalInterface
	public static interface Filter<A> {
		public boolean isValid(A target);
	}
}