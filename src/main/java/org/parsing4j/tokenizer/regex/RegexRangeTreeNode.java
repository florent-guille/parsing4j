package org.parsing4j.tokenizer.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
public class RegexRangeTreeNode {

	public RegexRange range;
	public int target;
	public RegexRangeTreeNode left, right;

	public int height;

	public RegexRangeTreeNode(RegexRange range, int target) {
		this.range = range;
		this.target = target;
	}

	public List<RegexRangeTreeNode> getChildren() {
		if (left == null && right == null) {
			return List.of();
		}

		if (left != null && right == null) {
			return List.of(left);
		}

		if (left == null && right != null) {
			return List.of(right);
		}
		return List.of(left, right);
	}
	
	@Override
	public String toString() {
		return range + " -> " + target;
	}

}