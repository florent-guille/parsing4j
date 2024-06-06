package org.parsing4j.tokenizer.regex.structure;

import java.util.List;

public class RegexClass extends Regex {

	private List<RegexRange> ranges;

	public RegexClass(List<RegexRange> ranges) {
		this.ranges = ranges;
	}

	public List<RegexRange> getRanges() {
		return ranges;
	}

	@Override
	public List<Regex> getChildren() {
		return List.of();
	}

	@Override
	public String getPrettyRepr() {
		return "Class(" + ranges + ")";
	}

}
