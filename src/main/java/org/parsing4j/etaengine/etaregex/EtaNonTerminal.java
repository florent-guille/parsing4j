package org.parsing4j.etaengine.etaregex;

import java.util.Objects;

public class EtaNonTerminal extends EtaSymbol {

	private String name;

	public EtaNonTerminal(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getPrettyRepr() {
		return "NT(" + name + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof EtaNonTerminal nonTerminal && Objects.equals(this.name, nonTerminal.name);
	}

}
