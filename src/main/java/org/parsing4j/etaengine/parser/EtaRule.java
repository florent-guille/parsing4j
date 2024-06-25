package org.parsing4j.etaengine.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.parsing4j.etaengine.regex.EtaSymbol;
import org.parsing4j.etaengine.regex.EtaVariable;

public class EtaRule {

	private String name;
	private EtaVariable variable;
	private List<EtaNode> nodes;
	private List<Map<EtaSymbol, Set<EtaNode>>> reverseTransitions;
	private Map<EtaNode, List<EtaNode>> reversedNodes;
	private int id;

	public EtaRule(String name, EtaVariable variable, List<EtaNode> nodes, Map<EtaNode, List<EtaNode>> reversedNodes,
			List<Map<EtaSymbol, Set<EtaNode>>> reverseTransitions) {
		this.name = name;
		this.variable = variable;
		this.nodes = nodes;
		this.reversedNodes = reversedNodes;
		this.reverseTransitions = reverseTransitions;
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