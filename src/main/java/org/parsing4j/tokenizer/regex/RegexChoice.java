package org.parsing4j.tokenizer.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
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
	public String getRepr() {
		return "Choice(" + choices.size() + ")";
	}

}