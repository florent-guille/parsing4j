package org.parsing4j.tokenizer.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
public class RegexFragment extends Regex {

	private String name;

	public RegexFragment(String name) {
		this.name = name;
	}

	@Override
	public List<Regex> getChildren() {
		return List.of();
	}

	@Override
	public String getRepr() {
		return "Fragment(" + name + ")";
	}

}