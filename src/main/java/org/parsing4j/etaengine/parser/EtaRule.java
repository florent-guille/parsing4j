package org.parsing4j.etaengine.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.parsing4j.core.Tag;
import org.parsing4j.etaengine.regex.EtaSymbol;
import org.parsing4j.etaengine.regex.EtaVariable;

/*
 * @author Florent Guille
 * */
public class EtaRule {

	private String name;
	private EtaVariable variable;
	private List<EtaNode> nodes;
	private List<Map<EtaSymbol, Set<EtaNode>>> reverseTransitions;
	private Map<EtaNode, List<EtaNode>> reversedNodes;
	private int id;

	private Map<String, List<Tag>> tags;

	public EtaRule(String name, EtaVariable variable, List<EtaNode> nodes, Map<EtaNode, List<EtaNode>> reversedNodes,
			List<Map<EtaSymbol, Set<EtaNode>>> reverseTransitions) {
		this.name = name;
		this.variable = variable;
		this.nodes = nodes;
		this.reversedNodes = reversedNodes;
		this.reverseTransitions = reverseTransitions;
		this.tags = new HashMap<>();
	}

	public Map<String, List<Tag>> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = new HashMap<>();

		for (Tag tag : tags) {
			if (!this.tags.containsKey(tag.name())) {
				this.tags.put(tag.name(), new ArrayList<>());
			}

			this.tags.get(tag.name()).add(tag);
		}
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public List<Map<EtaSymbol, Set<EtaNode>>> getReverseTransitions() {
		return reverseTransitions;
	}

	public Map<EtaNode, List<EtaNode>> getReversedNodes() {
		return reversedNodes;
	}

	public String getName() {
		return name;
	}

	public EtaVariable getVariable() {
		return variable;
	}

	public List<EtaNode> getNodes() {
		return nodes;
	}
}