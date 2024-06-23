package org.parsing4j.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Florent Guile
 */
public class Utils {

	/**
	 * Computes a representation of the tree whose root node is the given item
	 * 
	 * @param <T>  The type of the nodes to be considered as this tree's nodes
	 * @param root The root node of the tree
	 */
	public static <T> String toStringTree(T root, Function<? super T, ? extends String> nameGetter,
			Function<? super T, ? extends List<T>> childrenGetter, String rootHeadPrefix, String rootBodyPrefix) {
		StringBuilder builder = new StringBuilder();

		Deque<T> itemDeque = new ArrayDeque<>();
		Deque<String> headPrefixDeque = new ArrayDeque<>();
		Deque<String> bodyPrefixDeque = new ArrayDeque<>();

		itemDeque.push(root);
		headPrefixDeque.push(rootHeadPrefix);
		bodyPrefixDeque.push(rootBodyPrefix);

		while (!itemDeque.isEmpty()) {
			T item = itemDeque.pop();
			String headPrefix = headPrefixDeque.pop();
			String bodyPrefix = bodyPrefixDeque.pop();

			String name = nameGetter.apply(item);
			List<T> children = childrenGetter.apply(item);

			// Appending the line corresponding to this item
			builder.append(headPrefix);
			builder.append(name);

			String coreHeadPrefix = bodyPrefix + "├─";
			String coreBodyPrefix = bodyPrefix + "│ ";

			String tailHeadPrefix = bodyPrefix + "└─";
			String tailBodyPrefix = bodyPrefix + "  ";

			for (int i = children.size() - 1; i >= 0; i--) {
				itemDeque.push(children.get(i));
				if (i == children.size() - 1) {
					headPrefixDeque.push(tailHeadPrefix);
					bodyPrefixDeque.push(tailBodyPrefix);
				} else {
					headPrefixDeque.push(coreHeadPrefix);
					bodyPrefixDeque.push(coreBodyPrefix);
				}
			}

			if (!itemDeque.isEmpty()) {
				builder.append('\n');
			}
		}

		return builder.toString();
	}

	public static <T> String toStringTree(T root, Function<? super T, ? extends String> nameGetter,
			Function<? super T, ? extends List<T>> childrenGetter) {
		return toStringTree(root, nameGetter, childrenGetter, "", "");
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

	public static class Triplet<A, B, C> {
		public A left;
		public B middle;
		public C right;

		public Triplet(A left, B middle, C right) {
			this.left = left;
			this.middle = middle;
			this.right = right;
		}

		@Override
		public int hashCode() {
			return Objects.hash(left, middle, right);
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Triplet t && Objects.equals(this.left, t.left) && Objects.equals(this.middle, t.middle)
					&& Objects.equals(this.right, t.right);
		}

		@Override
		public String toString() {
			return "(" + left + "," + middle + "," + right + ")";
		}
	}

	@FunctionalInterface
	public static interface Filter<A> {
		public boolean isValid(A target);
	}

	public static int getDigitValue(char c, int base) {
		int result = -1;
		if (c >= 0x30 && c <= 0x39) {
			result = c - 0x30;
		}

		if (c >= 0x41 && c <= 0x5A) {
			result = c - 0x41 + 10;
		}

		if (c >= 0x61 && c <= 0x7D) {
			result = c - 0x61 + 10;
		}

		if (result >= base) {
			result = -1;
		}
		return result;
	}
}
