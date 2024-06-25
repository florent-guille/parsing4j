package org.parsing4j.etaengine.regex;

import java.util.Objects;

public class EtaTerminal extends EtaSymbol {

	public EtaTerminal(String name) {
		super(name);
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaTerminal t && Objects.equals(this.name, t.name);
	}
}
