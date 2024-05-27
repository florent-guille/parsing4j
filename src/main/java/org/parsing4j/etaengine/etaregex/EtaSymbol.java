package org.parsing4j.etaengine.etaregex;

import java.util.List;

public abstract class EtaSymbol extends EtaRegex {

	private int id = -1;

	@Override
	public List<EtaRegex> getChildren() {
		return List.of();
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}