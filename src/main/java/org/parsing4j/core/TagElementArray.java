package org.parsing4j.core;

import java.util.ArrayList;
import java.util.List;

/*
 * @author Florent Guille
 * */
public class TagElementArray extends TagElement {

	private List<TagElement> values;

	public TagElementArray() {
		this.values = new ArrayList<>();
	}

	public TagElementArray(List<TagElement> values) {
		this.values = values;
	}

	public TagElement get(int index) {
		return values.get(index);
	}

	public List<TagElement> getValues() {
		return values;
	}

}
