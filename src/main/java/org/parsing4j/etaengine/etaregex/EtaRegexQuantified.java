package org.parsing4j.etaengine.etaregex;

import java.util.List;

public class EtaRegexQuantified extends EtaRegex {

	private int quantifier;
	private EtaRegex target;

	public EtaRegexQuantified(int quantifier, EtaRegex target) {
		this.quantifier = quantifier;
		this.target = target;
	}

	public int getQuantifier() {
		return quantifier;
	}

	@Override
	public List<EtaRegex> getChildren() {
		return List.of(target);
	}

	@Override
	public String getPrettyRepr() {
		return "Quantified(" + (char) quantifier + ")";
	}

}