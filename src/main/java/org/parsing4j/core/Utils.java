package org.parsing4j.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
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
}
