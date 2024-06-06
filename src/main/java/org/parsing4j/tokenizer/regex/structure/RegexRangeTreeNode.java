package org.parsing4j.tokenizer.regex.structure;

public class RegexRangeTreeNode {

	public RegexRange range;
	public int target;
	public RegexRangeTreeNode left, right;

	public int height;

	public RegexRangeTreeNode(RegexRange range, int target) {
		this.range = range;
		this.target = target;
	}

}
