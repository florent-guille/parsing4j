package org.parsing4j.core;

/*
 * @author Florent Guille
 * */
public class IntIdentifiable {

	protected int id;

	public IntIdentifiable(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof IntIdentifiable i && this.id == i.id;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + id + ")";
	}

}