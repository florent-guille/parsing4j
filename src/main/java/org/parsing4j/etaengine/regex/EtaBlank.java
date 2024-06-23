package org.parsing4j.etaengine.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
public class EtaBlank extends EtaRegex{

	@Override
	public List<EtaRegex> getChildren() {
		return List.of();
	}

	@Override
	public String getRepr() {
		return "EtaBlank";
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaBlank;
	}

}
