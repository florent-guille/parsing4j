package org.parsing4j.tokenizer.regex.structure;

import java.util.List;

public class RegexChoice extends Regex {

	private List<Regex> choices;

	public RegexChoice(List<Regex> choices) {
		this.choices = choices;
	}

	@Override
	public List<Regex> getChildren() {
		return choices;
	}

	@Override
	public String getPrettyRepr() {
		return "Choice(" + choices.size() + ")";
	}

}
