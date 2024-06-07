package org.parsing4j.etaengine.etaparser;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.CharFlow.CharFlowException;
import org.parsing4j.core.Utils.Pair;
import org.parsing4j.etaengine.etaparser.EtaParser.EtaParserAction;
import org.parsing4j.etaengine.etaregex.EtaNonTerminal;
import org.parsing4j.etaengine.etaregex.EtaRegex;
import org.parsing4j.etaengine.etaregex.EtaRegexBlank;
import org.parsing4j.etaengine.etaregex.EtaRegexChoice;
import org.parsing4j.etaengine.etaregex.EtaRegexParser;
import org.parsing4j.etaengine.etaregex.EtaRegexQuantified;
import org.parsing4j.etaengine.etaregex.EtaRegexSequence;
import org.parsing4j.etaengine.etaregex.EtaSymbol;
import org.parsing4j.etaengine.etaregex.EtaTerminal;

public class EtaParserBuilder {

	// Input data
	private Map<String, Pair<String, EtaRegex>> rawRules;

	// Computed data
	private Map<String, EtaTerminal> terminalsMap;
	private Map<String, EtaNonTerminal> nonTerminalsMap;
	private Map<EtaNonTerminal, Set<EtaRule>> generators;
	private Map<String, EtaRule> rules;
	private Map<EtaSymbol, Set<EtaTerminal>> firstSets;
	private List<EtaParserBuilderState> builderStates;
	private EtaNonTerminal startVariable;
	private EtaTerminal empty, eof;

	public EtaParserBuilder() {
		this.rawRules = new HashMap<>();

		this.terminalsMap = new HashMap<>();
	}

	public EtaParserBuilder(Map<String, Pair<String, EtaRegex>> rawRules) {
		this.rawRules = rawRules;
		this.terminalsMap = new HashMap<>();
	}

	public void addRawRule(String name, String variable, EtaRegex regex) {
		this.rawRules.put(name, new Pair<>(variable, regex));
	}

	public void addRawRule(String name, String variable, String regex) throws IOException, CharFlowException {
		this.rawRules.put(name, new Pair<>(variable, EtaRegexParser.readEtaRegex(new CharFlow(regex))));
	}

	public EtaTerminal getTerminal(String name) {
		if (!terminalsMap.containsKey(name)) {
			terminalsMap.put(name, new EtaTerminal(name));
		}
		return terminalsMap.get(name);
	}

	public EtaNonTerminal getNonTerminal(String name) {
		if (!nonTerminalsMap.containsKey(name)) {
			nonTerminalsMap.put(name, new EtaNonTerminal(name));
		}
		return nonTerminalsMap.get(name);
	}

	public void buildRules() {
		this.nonTerminalsMap = new HashMap<>();
		this.rules = new HashMap<>();
		this.generators = new HashMap<>();

		for (Entry<String, Pair<String, EtaRegex>> entry : rawRules.entrySet()) {

			Deque<EtaRegex> input = new ArrayDeque<>();
			Deque<EtaRegex> unfolded = new ArrayDeque<>();

			input.push(entry.getValue().right);

			while (!input.isEmpty()) {
				EtaRegex current = input.pop();
				unfolded.push(current);
				for (EtaRegex child : current.getChildren()) {
					input.push(child);
				}
			}

			Deque<EtaBuildNode> buildNodes = new ArrayDeque<>();
			Supplier<EtaBuildNode> nodeSupplier = () -> {
				EtaBuildNode result = new EtaBuildNode(buildNodes.size());
				buildNodes.push(result);
				return result;
			};

			Deque<EtaBuildNode> starts = new ArrayDeque<>();
			Deque<EtaBuildNode> ends = new ArrayDeque<>();

			while (!unfolded.isEmpty()) {
				EtaRegex current = unfolded.pop();

				if (current instanceof EtaTerminal term) {
					EtaBuildNode start = nodeSupplier.get();
					EtaBuildNode end = nodeSupplier.get();
					start.addTransition(getTerminal(term.getName()), end);

					starts.push(start);
					ends.push(end);
					continue;
				}

				if (current instanceof EtaNonTerminal nonTerm) {
					EtaBuildNode start = nodeSupplier.get();
					EtaBuildNode end = nodeSupplier.get();
					start.addTransition(getNonTerminal(nonTerm.getName()), end);

					starts.push(start);
					ends.push(end);
					continue;
				}

				if (current instanceof EtaRegexChoice choice) {
					EtaBuildNode globalStart = nodeSupplier.get();
					EtaBuildNode globalEnd = nodeSupplier.get();

					List<EtaRegex> children = choice.getChildren();
					for (int i = 0; i < children.size(); i++) {
						EtaBuildNode start = starts.pop();
						EtaBuildNode end = ends.pop();

						globalStart.addEpsilonTransition(start);
						end.addEpsilonTransition(globalEnd);
					}

					starts.push(globalStart);
					ends.push(globalEnd);
					continue;
				}

				if (current instanceof EtaRegexBlank) {
					EtaBuildNode node = nodeSupplier.get();
					starts.push(node);
					ends.push(node);
					continue;
				}

				if (current instanceof EtaRegexSequence seq) {
					EtaBuildNode globalEnd = ends.pop();
					EtaBuildNode previousStart = starts.pop();

					List<EtaRegex> children = seq.getChildren();

					for (int i = 0; i < children.size() - 1; i++) {
						EtaBuildNode start = starts.pop();
						EtaBuildNode end = ends.pop();
						end.addEpsilonTransition(previousStart);
						previousStart = start;
					}

					starts.push(previousStart);
					ends.push(globalEnd);
					continue;
				}

				if (current instanceof EtaRegexQuantified quant) {
					EtaBuildNode start = starts.pop();
					EtaBuildNode end = ends.pop();

					if (quant.getQuantifier() == '*') {
						start.addEpsilonTransition(end);
						end.addEpsilonTransition(start);
					}

					if (quant.getQuantifier() == '+') {
						end.addEpsilonTransition(start);
					}

					if (quant.getQuantifier() == '?') {
						start.addEpsilonTransition(end);
					}

					starts.push(start);
					ends.push(end);
					continue;
				}
			}

			EtaBuildNode start = starts.pop();
			EtaBuildNode end = ends.pop();
			end.setIsFinal(true);

//			for (EtaBuildNode node : buildNodes) {
//				System.out.println(node.getGraphvizRepr(entry.getKey() + "/B"));
//			}

			assert starts.isEmpty() && ends.isEmpty();

			List<EtaNode> nodes = EtaBuildNode.determinize(start, new ArrayList<>(buildNodes.reversed()));

//			for (EtaNode node : nodes) {
//				System.out.println(node.getGraphvizRepr(entry.getKey() + "/F"));
//			}

			List<EtaBuildNode> reversedBuildNodes = new ArrayList<>();
			List<Map<EtaSymbol, Set<EtaNode>>> reverseTransitions = new ArrayList<>();
			for (EtaNode node : nodes) {
				reversedBuildNodes.add(new EtaBuildNode(node.getId()));
				reverseTransitions.add(new HashMap<>());
			}

			for (EtaNode node : nodes) {

				for (Entry<EtaSymbol, EtaNode> transition : node.getTransitions().entrySet()) {
					reversedBuildNodes.get(transition.getValue().getId()).addTransition(transition.getKey(),
							reversedBuildNodes.get(node.getId()));

					Map<EtaSymbol, Set<EtaNode>> currentReverse = reverseTransitions.get(transition.getValue().getId());
					if (!currentReverse.containsKey(transition.getKey())) {
						currentReverse.put(transition.getKey(), new HashSet<>());
					}
					currentReverse.get(transition.getKey()).add(node);
				}
			}

			Map<EtaNode, List<EtaNode>> reversedNodes = new HashMap<>();
			for (EtaNode node : nodes) {
				if (node.isFinal()) {
					reversedNodes.put(node,
							EtaBuildNode.determinize(reversedBuildNodes.get(node.getId()), reversedBuildNodes));
				}
			}
			EtaRule rule = new EtaRule(entry.getKey(), getNonTerminal(entry.getValue().left), nodes, reversedNodes,
					reverseTransitions);
			this.rules.put(entry.getKey(), rule);

			if (!generators.containsKey(rule.getVariable())) {
				generators.put(rule.getVariable(), new HashSet<>());
			}
			generators.get(rule.getVariable()).add(rule);
		}

		for (EtaNonTerminal nonTerminal : nonTerminalsMap.values()) {
			if (!generators.containsKey(nonTerminal)) {
				generators.put(nonTerminal, Set.of());
			}
		}

	}

	public void computeFirstSets() {
		this.firstSets = new HashMap<>();
		this.eof = this.getTerminal("eof_symbol");
		this.empty = new EtaTerminal("empty_symbol");

		for (EtaTerminal terminal : terminalsMap.values()) {
			firstSets.put(terminal, Set.of(terminal));
		}

		for (EtaNonTerminal nonTerminal : nonTerminalsMap.values()) {
			firstSets.put(nonTerminal, new HashSet<>());
		}

		boolean changed = true;
		while (changed) {
			changed = false;

			for (EtaRule rule : rules.values()) {
				Deque<EtaNode> nodeStack = new ArrayDeque<>();
				Set<EtaTerminal> currentSet = firstSets.get(rule.getVariable());
				Set<EtaNode> done = new HashSet<>();

				nodeStack.push(rule.getNodes().get(0));
				done.add(rule.getNodes().get(0));

				while (!nodeStack.isEmpty()) {
					EtaNode currentNode = nodeStack.pop();
					if (currentNode.isFinal()) {
						changed |= currentSet.add(empty);
					}

					for (Entry<EtaSymbol, EtaNode> transition : currentNode.getTransitions().entrySet()) {
						Set<EtaTerminal> partial = firstSets.get(transition.getKey());

						if (partial.contains(empty)) {
							partial = new HashSet<>(partial);
							partial.remove(empty);

							if (!done.contains(transition.getValue())) {
								nodeStack.push(transition.getValue());
								done.add(transition.getValue());
							}
						}
						changed |= currentSet.addAll(partial);
					}
				}
			}
		}

	}

	public void computeStates(String startVariableName) {
		startVariable = getNonTerminal(startVariableName);

		Map<EtaParserBuilderState, EtaParserBuilderState> builderStatesMap = new HashMap<>();
		builderStates = new ArrayList<>();
		Deque<EtaParserBuilderState> stack = new ArrayDeque<>();

		Set<EtaMarkedRule> rules = new HashSet<>();
		for (EtaRule rule : generators.get(startVariable)) {
			EtaMarkedRule markedRule = new EtaMarkedRule(new EtaConstrainedRule(rule, Set.of(eof)), 0);
			markedRule.getConstrainedRule().computeFollowSets(firstSets, empty);
			rules.add(markedRule);
		}

		EtaParserBuilderState rootState = new EtaParserBuilderState(rules);
		rootState.computeClosure(firstSets, empty, generators);
		rootState.simplify(firstSets, empty);
		builderStatesMap.put(rootState, rootState);
		builderStates.add(rootState);
		stack.push(rootState);

		while (!stack.isEmpty()) {
			EtaParserBuilderState currentState = stack.pop();

			Map<EtaSymbol, Set<EtaMarkedRule>> buildTransitions = new HashMap<>();

			for (EtaMarkedRule markedRule : currentState.getMarkedRules()) {
				for (Entry<EtaSymbol, EtaNode> transition : markedRule.getCurrentNode().getTransitions().entrySet()) {
					if (!buildTransitions.containsKey(transition.getKey())) {
						buildTransitions.put(transition.getKey(), new HashSet<>());
					}

					EtaMarkedRule next = new EtaMarkedRule(markedRule.getConstrainedRule(),
							transition.getValue().getId());
					buildTransitions.get(transition.getKey()).add(next);
				}
			}

			Map<EtaSymbol, EtaParserBuilderState> resultTransitions = new HashMap<>();

			for (Entry<EtaSymbol, Set<EtaMarkedRule>> transition : buildTransitions.entrySet()) {
				EtaParserBuilderState targetState = new EtaParserBuilderState(transition.getValue());
				targetState.computeClosure(firstSets, empty, generators);
				targetState.simplify(firstSets, empty);

				EtaParserBuilderState existing = builderStatesMap.get(targetState);
				if (existing == null) {
					existing = targetState;
					targetState.setId(builderStates.size());
					builderStatesMap.put(targetState, targetState);
					builderStates.add(targetState);
					stack.push(targetState);
				}
				resultTransitions.put(transition.getKey(), existing);
			}

			currentState.setTransitions(resultTransitions);

		}

	}

	public EtaParser createParser() throws ParserConflictException {
		int index = 0;
		for (EtaTerminal terminal : terminalsMap.values()) {
			terminal.setId(index++);
		}

		index = 0;
		for (EtaNonTerminal nonTerminal : nonTerminalsMap.values()) {
			nonTerminal.setId(index++);
		}

		index = 0;
		for (EtaRule rule : rules.values()) {
			rule.setId(index++);
		}

		List<EtaParserState> states = new ArrayList<>(builderStates.size());

		for (EtaParserBuilderState builderState : builderStates) {
			EtaParserAction[] actions = new EtaParserAction[terminalsMap.size()];
			int[] gotos = new int[nonTerminalsMap.size()];
			Set<EtaRule> activeRules = new HashSet<>();

			for (Entry<EtaSymbol, EtaParserBuilderState> transition : builderState.getTransitions().entrySet()) {
				if (transition.getKey() instanceof EtaTerminal terminal) {
					actions[terminal.getId()] = new EtaParserAction(EtaParserAction.SHIFT,
							transition.getValue().getId(), null, 0);
				}

				if (transition.getKey() instanceof EtaNonTerminal nonTerminal) {
					gotos[nonTerminal.getId()] = transition.getValue().getId();
				}
			}

			for (EtaMarkedRule markedRule : builderState.getMarkedRules()) {
				activeRules.add(markedRule.getConstrainedRule().getRule());

				if (!markedRule.getCurrentNode().isFinal()) {
					continue;
				}

				if (markedRule.getConstrainedRule().getRule().getVariable().equals(startVariable)) {
					for (EtaTerminal lookAhead : markedRule.getConstrainedRule().getLookAheads()) {
						actions[lookAhead.getId()] = checkForConflict(builderState, actions[lookAhead.getId()],
								new EtaParserAction(EtaParserAction.ACCEPT, 0, null, 0), lookAhead);
					}
					continue;
				}

				for (EtaTerminal lookAhead : markedRule.getCurrentFollowSet()) {
					if (markedRule.getCurrentNode().getTransitions().containsKey(lookAhead))
						continue;
					actions[lookAhead.getId()] = checkForConflict(builderState, actions[lookAhead.getId()],
							new EtaParserAction(EtaParserAction.REDUCE, 0, markedRule.getConstrainedRule().getRule(),
									markedRule.getIndex()),
							lookAhead);
				}
			}

			states.add(new EtaParserState(builderState.getId(), actions, gotos, activeRules));

		}

		return new EtaParser(states, terminalsMap, eof);
	}

	public EtaParser build(String startVariableName) throws ParserConflictException {
		buildRules();
		computeFirstSets();
		computeStates(startVariableName);
		return createParser();
	}

	public static final EtaParserAction checkForConflict(EtaParserBuilderState state, EtaParserAction current,
			EtaParserAction incoming, EtaTerminal lookAhead) throws ParserConflictException {
		if (current != null) {
			for (Entry<EtaSymbol, EtaParserBuilderState> transition : state.getTransitions().entrySet()) {
				System.out.println(transition.getKey() + " --> " + transition.getValue().getId());
			}
			for (EtaMarkedRule rule : state.getMarkedRules()) {
				System.out.println(rule.getIndex() + "," + rule.getConstrainedRule().getRule().getName());
				for (EtaNode node : rule.getConstrainedRule().getRule().getNodes()) {
					System.out.println(node.getGraphvizRepr("fail"));
				}
				System.out.println(rule.getConstrainedRule().getFollowSets());
			}
			throw new ParserConflictException(state, lookAhead, current, incoming);
		}

		return incoming;
	}

	public EtaTerminal getEOF() {
		return eof;
	}

	public EtaTerminal getEmpty() {
		return empty;
	}

	public Map<String, EtaRule> getRules() {
		return rules;
	}

	public Map<EtaSymbol, Set<EtaTerminal>> getFirstSets() {
		return firstSets;
	}

	public Map<EtaNonTerminal, Set<EtaRule>> getGenerators() {
		return generators;
	}

	@SuppressWarnings("serial")
	public static final class ParserConflictException extends Exception {

		private EtaParserBuilderState state;
		private EtaTerminal lookAhead;
		private EtaParserAction a1, a2;

		public ParserConflictException(EtaParserBuilderState state, EtaTerminal lookAhead, EtaParserAction a1,
				EtaParserAction a2) {
			super();
			this.state = state;
			this.lookAhead = lookAhead;
			this.a1 = a1;
			this.a2 = a2;
		}

		@Override
		public String getMessage() {
			return "Conflict between actions %s and %s for lookahead %s on state %s".formatted(a1, a2,
					lookAhead.getName(), state.getId());
		}
	}
}