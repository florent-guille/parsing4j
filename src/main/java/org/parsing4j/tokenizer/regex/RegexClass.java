package org.parsing4j.tokenizer.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
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
	public String getRepr() {
		return "Class(" + ranges + ")";
	}

}