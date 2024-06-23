package org.parsing4j.core;

/*
 * @author Florent Guille
 * */
public class TagElementBoolean extends TagElement {

	private boolean value;

	public TagElementBoolean(boolean value) {
		this.value = value;
	}

	@Override
	public boolean getAsBoolean() {
		return value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
