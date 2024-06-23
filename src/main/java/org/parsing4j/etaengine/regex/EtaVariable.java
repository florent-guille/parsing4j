package org.parsing4j.etaengine.regex;

import java.util.Objects;

/*
 * @author Florent Guille
 * */
public class EtaVariable extends EtaSymbol {

	private String name;

	public EtaVariable(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaVariable v && Objects.equals(this.name, v.name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof EtaVariable v && Objects.equals(this.name, v.name);
	}

	@Override
	public String toString() {
		return getRepr();
	}

	@Override
	public String getRepr() {
		return "EtaVariable(" + name + ")";
	}
}
