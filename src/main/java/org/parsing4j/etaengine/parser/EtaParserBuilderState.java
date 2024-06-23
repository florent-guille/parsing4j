package org.parsing4j.etaengine.parser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.parsing4j.etaengine.regex.EtaAbstractTerminal;
import org.parsing4j.etaengine.regex.EtaSymbol;
import org.parsing4j.etaengine.regex.EtaVariable;

/*
 * @author Florent Guille
 * */
public class EtaParserBuilderState {

	private Set<EtaMarkedRule> markedRules;
	private int id;
	private Map<EtaSymbol, EtaParserBuilderState> transitions;

	public EtaParserBuilderState(Set<EtaMarkedRule> markedRules) {
		this.markedRules = markedRules;
	}

	public void computeClosure(Map<EtaSymbol, Set<EtaAbstractTerminal>> firstSets,
			Map<EtaVariable, Set<EtaRule>> generators) {
		Deque<EtaMarkedRule> stack = new ArrayDeque<>();
		for (EtaMarkedRule rule : markedRules) {
			stack.push(rule);
		}

		while (!stack.isEmpty()) {
			EtaMarkedRule currentRule = stack.pop();

			for (Entry<EtaSymbol, EtaNode> transition : currentRule.getCurrentNode().getTransitions().entrySet()) {
				if (transition.getKey() instanceof EtaVariable nonTerminal) {

					for (EtaRule rule : generators.get(nonTerminal)) {
						EtaMarkedRule newRule = new EtaMarkedRule(new EtaConstrainedRule(rule,
								currentRule.getConstrainedRule().getFollowSets().get(transition.getValue().getId())),
								0);
						if (!markedRules.contains(newRule)) {
							newRule.getConstrainedRule().computeFollowSets(firstSets);
							markedRules.add(newRule);
							stack.add(newRule);
						}
					}

				}
			}
		}
	}

	public void simplify(Map<EtaSymbol, Set<EtaAbstractTerminal>> firstSets) {
		Map<EtaPointedRule, Set<EtaAbstractTerminal>> merged = new HashMap<>();

		for (EtaMarkedRule rule : markedRules) {
			EtaPointedRule pointed = new EtaPointedRule(rule.getConstrainedRule().getRule(), rule.getIndex());

			Set<EtaAbstractTerminal> lookAheads = merged.get(pointed);
			if (lookAheads == null) {
				lookAheads = new HashSet<>();
				merged.put(pointed, lookAheads);
			}
			lookAheads.addAll(rule.getConstrainedRule().getLookAheads());
		}

		this.markedRules = new HashSet<>();

		for (Entry<EtaPointedRule, Set<EtaAbstractTerminal>> item : merged.entrySet()) {
			EtaMarkedRule rule = new EtaMarkedRule(new EtaConstrainedRule(item.getKey().rule(), item.getValue()),
					item.getKey().index());
			rule.getConstrainedRule().computeFollowSets(firstSets);
			markedRules.add(rule);
		}
	}

	public void setTransitions(Map<EtaSymbol, EtaParserBuilderState> transitions) {
		this.transitions = transitions;
	}

	public Map<EtaSymbol, EtaParserBuilderState> getTransitions() {
		return transitions;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Set<EtaMarkedRule> getMarkedRules() {
		return markedRules;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(markedRules);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof EtaParserBuilderState state && Objects.equals(this.markedRules, state.markedRules);
	}

	private static record EtaPointedRule(EtaRule rule, int index) {
	}
}