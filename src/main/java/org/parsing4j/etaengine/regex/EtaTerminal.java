package org.parsing4j.etaengine.regex;

import java.util.Objects;

public class EtaTerminal extends EtaSymbol {

	public EtaTerminal(String name) {
		super(name);
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaTerminal t && Objects.equals(this.name, t.name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof EtaTerminal t && Objects.equals(this.name, t.name);
	}
}
