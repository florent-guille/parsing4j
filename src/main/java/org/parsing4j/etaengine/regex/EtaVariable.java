package org.parsing4j.etaengine.regex;

import java.util.Objects;

public class EtaVariable extends EtaSymbol {

	public EtaVariable(String name) {
		super(name);
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaVariable v && Objects.equals(this.name, v.name);
	}

}
