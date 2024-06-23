package org.parsing4j.tokenizer.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
public class RegexBlank extends Regex {

	public RegexBlank() {

	}

	@Override
	public List<Regex> getChildren() {
		return List.of();
	}

	@Override
	public String getRepr() {
		return "Blank";
	}

}