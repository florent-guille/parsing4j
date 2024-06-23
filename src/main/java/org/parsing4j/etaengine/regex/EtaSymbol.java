package org.parsing4j.etaengine.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
public abstract class EtaSymbol extends EtaRegex {

	protected int id = -1;

	public EtaSymbol() {

	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public List<EtaRegex> getChildren() {
		return List.of();
	}

}
