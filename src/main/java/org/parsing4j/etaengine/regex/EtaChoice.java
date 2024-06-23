package org.parsing4j.etaengine.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
public class EtaChoice extends EtaRegex {

	private List<EtaRegex> items;

	public EtaChoice(List<EtaRegex> items) {
		this.items = items;
	}

	@Override
	public List<EtaRegex> getChildren() {
		return items;
	}

	@Override
	public String getRepr() {
		return "EtaChoice(" + items.size() + ")";
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaChoice;
	}

}
