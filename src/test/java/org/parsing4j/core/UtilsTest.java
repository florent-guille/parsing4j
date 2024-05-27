package org.parsing4j.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void treeReprTest() {
		List<TreeNode> nodes = IntStream.range(0, 10).mapToObj(index -> new TreeNode("node_" + index)).toList();
		nodes.get(0).addChild(nodes.get(1));
		nodes.get(0).addChild(nodes.get(2));
		nodes.get(2).addChild(nodes.get(3));
		nodes.get(2).addChild(nodes.get(4));
		nodes.get(4).addChild(nodes.get(5));

		System.out.println(Utils.treeRepr(nodes.get(0), TreeNode::getRepr, TreeNode::getChildren));
	}

	public static class TreeNode {

		private String name;
		private List<TreeNode> children;

		public TreeNode(String name) {
			this.name = name;
			this.children = new ArrayList<>();
		}

		public void addChild(TreeNode node) {
			this.children.add(node);
		}

		public String getRepr() {
			return "TreeNode(" + name + ")";
		}

		public List<TreeNode> getChildren() {
			return children;
		}
	}

}