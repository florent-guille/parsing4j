package org.parsing4j.core;

/*
 * @author Florent Guille
 * */
public class TagElementInteger extends TagElement {

	private int value;

	public TagElementInteger(int value) {
		this.value = value;
	}

	@Override
	public int getAsInt() {
		return value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
