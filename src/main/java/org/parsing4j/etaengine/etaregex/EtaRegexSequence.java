package org.parsing4j.etaengine.etaregex;

import java.util.List;

public class EtaRegexSequence extends EtaRegex {

	private List<EtaRegex> sequence;

	public EtaRegexSequence(List<EtaRegex> sequence) {
		this.sequence = sequence;
	}

	@Override
	public List<EtaRegex> getChildren() {
		return sequence;
	}

	@Override
	public String getPrettyRepr() {
		return "Sequence(" + sequence.size() + ")";
	}

}
