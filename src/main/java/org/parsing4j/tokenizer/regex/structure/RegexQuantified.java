package org.parsing4j.tokenizer.regex.structure;

import java.util.List;

public class RegexQuantified extends Regex {

	private int quantifier;
	private Regex target;

	public RegexQuantified(int quantifier, Regex target) {
		this.quantifier = quantifier;
		this.target = target;
	}

	@Override
	public List<Regex> getChildren() {
		return List.of(target);
	}

	@Override
	public String getPrettyRepr() {
		return "Quantified(" + (char) quantifier + ")";
	}

}
