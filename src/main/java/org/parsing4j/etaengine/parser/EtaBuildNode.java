package org.parsing4j.etaengine.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.parsing4j.etaengine.regex.EtaSymbol;

/*
 * @author Florent Guille
 * */
public class EtaBuildNode {

	private int id;
	private boolean isFinal;
	private Map<EtaSymbol, Set<EtaBuildNode>> transitions;
	private Set<EtaBuildNode> epsilonTransitions;

	public EtaBuildNode(int id) {
		this.id = id;
		this.transitions = new HashMap<>();
		this.epsilonTransitions = new HashSet<>();
	}

	public int getId() {
		return id;
	}

	public void setIsFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public void addTransition(EtaSymbol symbol, EtaBuildNode target) {
		if (!transitions.containsKey(symbol)) {
			transitions.put(symbol, new HashSet<>());
		}
		transitions.get(symbol).add(target);
	}

	public void addEpsilonTransition(EtaBuildNode target) {
		epsilonTransitions.add(target);
	}

	public Map<EtaSymbol, Set<EtaBuildNode>> getTransitions() {
		return transitions;
	}

	public Set<EtaBuildNode> getEpsilonTransitions() {
		return epsilonTransitions;
	}

	public String getGraphvizRepr(String prefix) {
		StringBuilder builder = new StringBuilder();

		int total = epsilonTransitions.size();

		if (isFinal) {

			builder.append("\"");
			builder.append(prefix);
			builder.append(this.id);
			builder.append("\" [peripheries=2]");
			if (total > 0) {
				builder.append("\n");
			}
		}

		for (Set<EtaBuildNode> set : transitions.values()) {
			total += set.size();
		}

		for (EtaBuildNode target : epsilonTransitions) {
			builder.append("\"");
			builder.append(prefix);
			builder.append(this.id);
			builder.append("\" -> \"");
			builder.append(prefix);
			builder.append(target.id);
			builder.append("\" [label=\"epsilon\"]");

			total--;
			if (total > 0) {
				builder.append("\n");
			}
		}

		for (Entry<EtaSymbol, Set<EtaBuildNode>> entry : transitions.entrySet()) {
			for (EtaBuildNode target : entry.getValue()) {
				builder.append("\"");
				builder.append(prefix);
				builder.append(this.id);
				builder.append("\" -> \"");
				builder.append(prefix);
				builder.append(target.id);
				builder.append("\" [label=\"");
				builder.append(entry.getKey().getRepr());
				builder.append("\"]");
				
				total--;
				if (total > 0) {
					builder.append("\n");
				}
			}
		}

		return builder.toString();
	}

	public static List<EtaNode> determinize(EtaBuildNode buildRoot, List<EtaBuildNode> buildNodes) {
		List<Set<EtaBuildNode>> closures = new ArrayList<>(buildNodes.size());

		for (EtaBuildNode node : buildNodes) {
			closures.add(new HashSet<>(List.of(node)));
		}

		boolean changed = true;
		while (changed) {
			changed = false;
			for (EtaBuildNode node : buildNodes) {
				for (EtaBuildNode target : node.getEpsilonTransitions()) {
					changed |= closures.get(node.id).addAll(closures.get(target.id));
				}
			}
		}

		Map<Set<EtaBuildNode>, EtaNode> nodeMap = new HashMap<>();
		Deque<EtaNode> nodes = new ArrayDeque<>();

		Deque<Set<EtaBuildNode>> stack = new ArrayDeque<>();

		EtaNode root = new EtaNode(0);
		nodes.push(root);
		nodeMap.put(closures.get(buildRoot.id), root);
		stack.push(closures.get(buildRoot.id));

		while (!stack.isEmpty()) {
			Set<EtaBuildNode> currentSet = stack.pop();
			EtaNode currentNode = nodeMap.get(currentSet);

			Map<EtaSymbol, Set<EtaBuildNode>> mergedTransitions = new HashMap<>();

			for (EtaBuildNode node : currentSet) {
				if (node.isFinal) {
					currentNode.setIsFinal(true);
				}
				for (Entry<EtaSymbol, Set<EtaBuildNode>> entry : node.transitions.entrySet()) {
					if (!mergedTransitions.containsKey(entry.getKey())) {
						mergedTransitions.put(entry.getKey(), new HashSet<>());
					}
					mergedTransitions.get(entry.getKey()).addAll(entry.getValue());
				}
			}

			Map<EtaSymbol, EtaNode> transitions = new HashMap<>();

			for (Entry<EtaSymbol, Set<EtaBuildNode>> entry : mergedTransitions.entrySet()) {
				Set<EtaBuildNode> closed = new HashSet<>();
				for (EtaBuildNode node : entry.getValue()) {
					closed.addAll(closures.get(node.id));
				}

				EtaNode target = nodeMap.get(closed);
				if (target == null) {
					target = new EtaNode(nodes.size());
					nodes.push(target);
					nodeMap.put(closed, target);
					stack.push(closed);
				}
				transitions.put(entry.getKey(), target);
			}

			currentNode.setTransitions(transitions);
		}

		return new ArrayList<>(nodes.reversed());
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof EtaBuildNode node && this.id == node.id;
	}

	@Override
	public String toString() {
		return "EtaBuildNode(" + id + ")";
	}

}