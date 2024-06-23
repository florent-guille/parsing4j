package org.parsing4j.tokenizer.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
public class RegexQuantified extends Regex {

	private int quantifier;
	private Regex target;

	public RegexQuantified(int quantifier, Regex target) {
		this.quantifier = quantifier;
		this.target = target;
	}

	public int getQuantifier() {
		return quantifier;
	}

	@Override
	public List<Regex> getChildren() {
		return List.of(target);
	}

	@Override
	public String getRepr() {
		return "Quantified(" + (char) quantifier + ")";
	}

}