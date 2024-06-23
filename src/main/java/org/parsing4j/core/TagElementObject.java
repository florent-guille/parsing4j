package org.parsing4j.core;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * @author Florent Guille
 * */
public class TagElementObject extends TagElement {

	private Map<String, TagElement> data;

	public TagElementObject() {
		this.data = new HashMap<>();
	}

	public TagElementObject(Map<String, TagElement> data) {
		this.data = data;
	}

	public TagElement get(String name) {
		return data.get(name);
	}

	@Override
	public String toString() {
		return data.entrySet().stream().map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
				.collect(Collectors.joining(",", "{", "}"));
	}

}