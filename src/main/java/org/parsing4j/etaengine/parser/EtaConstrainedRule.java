package org.parsing4j.etaengine.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.parsing4j.etaengine.regex.EtaSymbol;
import org.parsing4j.etaengine.regex.EtaTerminal;

public class EtaConstrainedRule {

	private EtaRule rule;
	private Set<EtaTerminal> lookAheads;
	private List<Set<EtaTerminal>> followSets;

	public EtaConstrainedRule(EtaRule rule, Set<EtaTerminal> lookAheads) {
		this.rule = rule;
		this.lookAheads = lookAheads;
	}

	public void computeFollowSets(Map<EtaSymbol, Set<EtaTerminal>> firstSets, EtaTerminal empty) {
		this.followSets = new ArrayList<>(rule.getNodes().size());

		Deque<EtaNode> stack = new ArrayDeque<>();
		Set<EtaNode> onStack = new HashSet<>();

		for (EtaNode node : rule.getNodes()) {
			if (node.isFinal()) {
				followSets.add(new HashSet<>(lookAheads));
				stack.push(node);
				onStack.add(node);
			} else {
				followSets.add(new HashSet<>());
			}
		}

		while (!stack.isEmpty()) {
			EtaNode currentNode = stack.pop();
			onStack.remove(currentNode);

			for (Entry<EtaSymbol, Set<EtaNode>> reverseTransition : rule.getReverseTransitions()
					.get(currentNode.getId()).entrySet()) {

				Set<EtaTerminal> partial = firstSets.get(reverseTransition.getKey());

				if (partial.contains(empty)) {
					partial = new HashSet<>(partial);
					partial.remove(empty);
					partial.addAll(followSets.get(currentNode.getId()));
				}

				for (EtaNode target : reverseTransition.getValue()) {
					boolean changed = followSets.get(target.getId()).addAll(partial);
					if (changed && !onStack.contains(target)) {
						stack.push(target);
						onStack.add(target);
					}
				}
			}
		}

	}

	public Set<EtaTerminal> getLookAheads() {
		return lookAheads;
	}

	public EtaRule getRule() {
		return rule;
	}

	public List<Set<EtaTerminal>> getFollowSets() {
		return followSets;
	}

	@Override
	public int hashCode() {
		return Objects.hash(rule, lookAheads);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof EtaConstrainedRule crule && Objects.equals(this.rule, crule.rule)
				&& Objects.equals(this.lookAheads, crule.lookAheads);
	}

}