package org.parsing4j.etaengine.parser;

import java.util.Map;
import java.util.Map.Entry;

import org.parsing4j.etaengine.regex.EtaSymbol;

/*
 * @author Florent Guille
 * */
public class EtaNode {

	private int id;
	private Map<EtaSymbol, EtaNode> transitions;
	private boolean isFinal;

	public EtaNode(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setIsFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public void setTransitions(Map<EtaSymbol, EtaNode> transitions) {
		this.transitions = transitions;
	}

	public Map<EtaSymbol, EtaNode> getTransitions() {
		return transitions;
	}

	public String getGraphvizRepr(String prefix) {
		StringBuilder builder = new StringBuilder();
		if (isFinal) {
			builder.append("\"");
			builder.append(prefix);
			builder.append(id);
			builder.append("\" [peripheries=2]");
			if (transitions.size() > 0) {
				builder.append("\n");
			}
		}
		int total = transitions.size();
		for (Entry<EtaSymbol, EtaNode> entry : transitions.entrySet()) {
			builder.append("\"");
			builder.append(prefix);
			builder.append(id);
			builder.append("\" -> \"");
			builder.append(prefix);
			builder.append(entry.getValue().id);
			builder.append("\" [label=\"");
			builder.append(entry.getKey());
			builder.append("\"]");
			total--;
			if (total > 0) {
				builder.append("\n");
			}
		}

		return builder.toString();
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof EtaNode node && this.id == node.id;
	}

}