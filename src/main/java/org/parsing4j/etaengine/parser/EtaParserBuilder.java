package org.parsing4j.etaengine.parser;

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
import org.parsing4j.etaengine.parser.EtaParser.EtaParserAction;
import org.parsing4j.etaengine.regex.EtaBlank;
import org.parsing4j.etaengine.regex.EtaChoice;
import org.parsing4j.etaengine.regex.EtaQuantified;
import org.parsing4j.etaengine.regex.EtaQuantified.EtaQuantifier;
import org.parsing4j.etaengine.regex.EtaRegex;
import org.parsing4j.etaengine.regex.EtaRegexParser;
import org.parsing4j.etaengine.regex.EtaSequence;
import org.parsing4j.etaengine.regex.EtaSymbol;
import org.parsing4j.etaengine.regex.EtaTerminal;
import org.parsing4j.etaengine.regex.EtaVariable;

public class EtaParserBuilder {

	// Input data
	private List<EtaParserBuilderEntry> entries;

	// Computed data
	private Map<String, EtaTerminal> terminalsMap;
	private Map<String, EtaVariable> nonTerminalsMap;
	private Map<EtaVariable, Set<EtaRule>> generators;
	private List<EtaRule> rules;
	private Map<EtaSymbol, Set<EtaTerminal>> firstSets;
	private List<EtaParserBuilderState> builderStates;
	private EtaVariable startVariable;
	private EtaTerminal empty, eof;

	public EtaParserBuilder() {
		this.entries = new ArrayList<>();
		this.terminalsMap = new HashMap<>();
	}

	public void addRawRule(String name, String variable, EtaRegex regex) {
		this.entries.add(new EtaParserBuilderEntry(name, variable, regex));
	}

	public void addRawRule(String name, String variable, String regex) throws IOException, CharFlowException {
		addRawRule(name, variable, EtaRegexParser.parseEtaRegex(new CharFlow(regex)));
	}

	public EtaTerminal getTerminal(String name) {
		if (!terminalsMap.containsKey(name)) {
			terminalsMap.put(name, new EtaTerminal(name));
		}
		return terminalsMap.get(name);
	}

	public EtaVariable getNonTerminal(String name) {
		if (!nonTerminalsMap.containsKey(name)) {
			nonTerminalsMap.put(name, new EtaVariable(name));
		}
		return nonTerminalsMap.get(name);
	}

	public void buildRules() {
		this.nonTerminalsMap = new HashMap<>();
		this.rules = new ArrayList<>();
		this.generators = new HashMap<>();

		for (EtaParserBuilderEntry entry : entries) {

			Deque<EtaRegex> input = new ArrayDeque<>();
			Deque<EtaRegex> unfolded = new ArrayDeque<>();

			input.push(entry.regex());

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

				if (current instanceof EtaVariable nonTerm) {
					EtaBuildNode start = nodeSupplier.get();
					EtaBuildNode end = nodeSupplier.get();
					start.addTransition(getNonTerminal(nonTerm.getName()), end);

					starts.push(start);
					ends.push(end);
					continue;
				}

				if (current instanceof EtaChoice choice) {
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

				if (current instanceof EtaBlank) {
					EtaBuildNode node = nodeSupplier.get();
					starts.push(node);
					ends.push(node);
					continue;
				}

				if (current instanceof EtaSequence seq) {
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

				if (current instanceof EtaQuantified quant) {
					EtaBuildNode start = starts.pop();
					EtaBuildNode end = ends.pop();

					if (quant.getQuantifier() == EtaQuantifier.STAR) {
						start.addEpsilonTransition(end);
						end.addEpsilonTransition(start);
					}

					if (quant.getQuantifier() == EtaQuantifier.PLUS) {
						end.addEpsilonTransition(start);
					}

					if (quant.getQuantifier() == EtaQuantifier.QUESTION_MARK) {
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

			assert starts.isEmpty() && ends.isEmpty();

			List<EtaNode> nodes = EtaBuildNode.determinize(start, new ArrayList<>(buildNodes.reversed()));

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
			EtaRule rule = new EtaRule(entry.name(), getNonTerminal(entry.var()), nodes, reversedNodes,
					reverseTransitions);
			this.rules.add(rule);

			if (!generators.containsKey(rule.getVariable())) {
				generators.put(rule.getVariable(), new HashSet<>());
			}
			generators.get(rule.getVariable()).add(rule);
		}

		for (EtaVariable nonTerminal : nonTerminalsMap.values()) {
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

		for (EtaVariable nonTerminal : nonTerminalsMap.values()) {
			firstSets.put(nonTerminal, new HashSet<>());
		}

		boolean changed = true;
		while (changed) {
			changed = false;

			for (EtaRule rule : rules) {
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
		for (EtaVariable nonTerminal : nonTerminalsMap.values()) {
			nonTerminal.setId(index++);
		}

		index = 0;
		for (EtaRule rule : rules) {
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

				if (transition.getKey() instanceof EtaVariable nonTerminal) {
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

	public Map<EtaSymbol, Set<EtaTerminal>> getFirstSets() {
		return firstSets;
	}

	public Map<EtaVariable, Set<EtaRule>> getGenerators() {
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

	public static record EtaParserBuilderEntry(String name, String var, EtaRegex regex) {

	}
}