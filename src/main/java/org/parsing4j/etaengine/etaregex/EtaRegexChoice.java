package org.parsing4j.etaengine.etaregex;

import java.util.List;

public class EtaRegexChoice extends EtaRegex {

	private List<EtaRegex> choices;

	public EtaRegexChoice(List<EtaRegex> choices) {
		this.choices = choices;
	}

	@Override
	public List<EtaRegex> getChildren() {
		return choices;
	}

	@Override
	public String getPrettyRepr() {
		return "Choices(" + choices.size() + ")";
	}

}
