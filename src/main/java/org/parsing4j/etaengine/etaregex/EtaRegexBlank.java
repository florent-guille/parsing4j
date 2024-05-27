package org.parsing4j.etaengine.etaregex;

import java.util.List;

public class EtaRegexBlank extends EtaRegex {

	public static final EtaRegexBlank BLANK = new EtaRegexBlank();

	private EtaRegexBlank() {

	}

	@Override
	public List<EtaRegex> getChildren() {
		return List.of();
	}

	@Override
	public String getPrettyRepr() {
		return "Blank";
	}

}
