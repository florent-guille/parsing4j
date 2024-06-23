package org.parsing4j.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.junit.Test;

/*
 * @author Florent Guille
 * */
public class UtilsTest {

	@Test
	public void test$1() {
		List<DummyNode<String>> nodes = IntStream.range(0, 10).mapToObj(i -> "node_" + i).map(DummyNode::new).toList();

		nodes.get(0).children.add(nodes.get(1));
		nodes.get(0).children.add(nodes.get(2));
		nodes.get(0).children.add(nodes.get(4));

		nodes.get(1).children.add(nodes.get(3));
		nodes.get(1).children.add(nodes.get(6));

		nodes.get(2).children.add(nodes.get(7));

		nodes.get(4).children.add(nodes.get(8));

		String stringTree = Utils.toStringTree(nodes.get(0), Objects::toString, DummyNode::getChildren);
		assertEquals(//
				"DummyNode(node_0)\n" + 
						"├─DummyNode(node_1)\n" + //
						"│ ├─DummyNode(node_3)\n" + //
						"│ └─DummyNode(node_6)\n" + //
						"├─DummyNode(node_2)\n" + //
						"│ └─DummyNode(node_7)\n" + //
						"└─DummyNode(node_4)\n" + // s
						"  └─DummyNode(node_8)",
				stringTree);
	}

	private static class DummyNode<T> {

		private T data;
		private List<DummyNode<T>> children;

		private DummyNode(T data, List<DummyNode<T>> children) {
			this.data = data;
			this.children = children;
		}

		private DummyNode(T data) {
			this(data, new ArrayList<>());
		}

		private List<DummyNode<T>> getChildren() {
			return children;
		}

		@Override
		public String toString() {
			return "DummyNode(" + Objects.toString(data) + ")";
		}

	}

}
