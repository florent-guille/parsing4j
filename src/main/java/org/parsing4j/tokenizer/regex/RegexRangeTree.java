package org.parsing4j.tokenizer.regex;

public class RegexRangeTree {

	private RegexRangeTreeNode root;

	public RegexRangeTree() {

	}

	public void insertRange(RegexRange range, int target) {
		root = insert(root, range, target);
	}

	public int search(int c) {
		RegexRangeTreeNode current = root;

		while (current != null) {
			int comp = current.range.compareTo(c);

			if (comp > 0) {
				current = current.left;
				break;
			}

			if (comp < 0) {
				current = current.right;
				break;
			}

			return current.target;
		}
		return -1;
	}

	private static int height(RegexRangeTreeNode node) {
		return node == null ? -1 : node.height;
	}

	private static void updateHeight(RegexRangeTreeNode node) {
		node.height = 1 + Math.max(height(node.left), height(node.right));
	}

	private static int balanceFactor(RegexRangeTreeNode node) {
		return node == null ? 0 : height(node.right) - height(node.left);
	}

	private static RegexRangeTreeNode rotateRight(RegexRangeTreeNode node) {
		RegexRangeTreeNode x = node.left;
		RegexRangeTreeNode z = x.right;

		x.right = node;
		node.left = z;
		updateHeight(x);
		updateHeight(node);
		return x;
	}

	private static RegexRangeTreeNode rotateLeft(RegexRangeTreeNode node) {
		RegexRangeTreeNode x = node.right;
		RegexRangeTreeNode z = x.left;

		x.left = node;
		node.right = z;
		updateHeight(x);
		updateHeight(node);
		return x;
	}

	private static RegexRangeTreeNode rebalance(RegexRangeTreeNode node) {
		int factor = balanceFactor(node);
		if (factor > 1) {
			if (balanceFactor(node.right) < 0) {
				node.right = rotateRight(node.right);
			}

			return rotateLeft(node);
		}

		if (factor < -1) {
			if (balanceFactor(node.left) > 0) {
				node.left = rotateLeft(node.left);
			}
			return rotateRight(node);
		}
		return node;

	}

	private static RegexRangeTreeNode insert(RegexRangeTreeNode node, RegexRange range, int target) {
		if (node == null) {
			return new RegexRangeTreeNode(range, target);
		}

		int comp = range.compareTo(node.range);

		if (comp > 0) {
			node.right = insert(node.right, range, target);
			return rebalance(node);
		}

		if (comp < 0) {
			node.left = insert(node.left, range, target);
			return rebalance(node);
		}

		return node;
	}

	private static class RegexRangeTreeNode {

		private RegexRange range;
		private int target;
		private RegexRangeTreeNode left, right;

		private int height;

		public RegexRangeTreeNode(RegexRange range, int target) {
			this.range = range;
			this.target = target;
		}

	}

}
