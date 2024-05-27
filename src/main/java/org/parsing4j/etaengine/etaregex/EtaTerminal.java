package org.parsing4j.etaengine.etaregex;

import java.util.Objects;

public class EtaTerminal extends EtaSymbol {

	private String name;

	public EtaTerminal(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getPrettyRepr() {
		return "T(" + name + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof EtaTerminal terminal && Objects.equals(this.name, terminal.name);
	}

}