package org.parsing4j.etaengine.regex;

import java.util.List;

public abstract class EtaSymbol extends EtaRegex {

	protected String name;
	protected int id;

	public EtaSymbol(String name) {
		this.name = name;
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

	@Override
	public String getRepr() {
		return this.getClass().getSimpleName() + "(" + name + ")";
	}

}
