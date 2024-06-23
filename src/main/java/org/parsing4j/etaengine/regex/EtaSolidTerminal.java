package org.parsing4j.etaengine.regex;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/*
 * @author Florent Guille
 * */
public class EtaSolidTerminal extends EtaAbstractTerminal {

	private String name;
	private Set<String> tags;

	public EtaSolidTerminal(String name) {
		super();
		this.tags = new HashSet<>();
		this.name = name;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void addTag(String tag) {
		this.tags.add(tag);
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaSolidTerminal t && Objects.equals(this.name, t.name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof EtaSolidTerminal t && Objects.equals(this.name, t.name);
	}

	@Override
	public String toString() {
		return getRepr();
	}

	@Override
	public String getRepr() {
		return "EtaTerminal(" + name + ")";
	}
}
