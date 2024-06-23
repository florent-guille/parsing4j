package org.parsing4j.etaengine.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
public class EtaSequence extends EtaRegex {

	private List<EtaRegex> items;

	public EtaSequence(List<EtaRegex> items) {
		this.items = items;
	}

	@Override
	public List<EtaRegex> getChildren() {
		return items;
	}

	@Override
	public String getRepr() {
		if (items.isEmpty()) {
			return "EtaBlank";
		}
		return "EtaSequence(" + items.size() + ")";
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaSequence;
	}

}
