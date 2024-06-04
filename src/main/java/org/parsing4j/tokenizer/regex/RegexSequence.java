package org.parsing4j.tokenizer.regex;

import java.util.List;

public class RegexSequence extends Regex {

	private List<Regex> sequence;

	public RegexSequence(List<Regex> sequence) {
		this.sequence = sequence;
	}

	@Override
	public List<Regex> getChildren() {
		return sequence;
	}

	@Override
	public String getPrettyRepr() {
		return "Sequence(" + sequence.size() + ")";
	}

}
