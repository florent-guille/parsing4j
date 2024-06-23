package org.parsing4j.core;

/*
 * @author Florent Guille
 * */
public class TagElementString extends TagElement {

	private String value;

	public TagElementString(String value) {
		this.value = value;
	}

	@Override
	public String getAsString() {
		return value;
	}

	@Override
	public String toString() {
		return '"' + value + '"';
	}

}
