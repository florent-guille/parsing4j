package org.parsing4j.tokenizer.regex.structure;

import java.util.List;

public class RegexBlank extends Regex {

	public RegexBlank() {

	}

	@Override
	public List<Regex> getChildren() {
		return List.of();
	}

	@Override
	public String getPrettyRepr() {
		return "Blank";
	}

}
