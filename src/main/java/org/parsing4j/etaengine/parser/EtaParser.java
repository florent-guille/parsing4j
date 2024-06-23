package org.parsing4j.etaengine.parser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.parsing4j.core.PushbackIterator;
import org.parsing4j.core.Token;
import org.parsing4j.etaengine.regex.EtaSolidTerminal;
import org.parsing4j.etaengine.regex.EtaSymbol;
import org.parsing4j.parser.Parser;

/*
 * @author Florent Guille
 * */
public class EtaParser extends Parser {

	private List<EtaParserState> states;
	private Map<String, EtaSolidTerminal> terminalsMap;
	private ParserReducer reducer;

	public EtaParser(List<EtaParserState> states, Map<String, EtaSolidTerminal> terminalsMap) {
		this.states = states;
		this.terminalsMap = terminalsMap;
		this.reducer = ParsedNode::new;
	}

	public EtaSolidTerminal getTerminal(String name) {
		if (!terminalsMap.containsKey(name)) {
			terminalsMap.put(name, new EtaSolidTerminal(name));
		}
		return terminalsMap.get(name);
	}

	public List<EtaParserState> getStates() {
		return states;
	}

	public void setReducer(ParserReducer reducer) {
		this.reducer = reducer;
	}

	public Object parse(Iterator<Token> iterator) throws Exception {
		PushbackIterator<Token> tokenflow = new PushbackIterator<>(iterator);

		Deque<EtaParserState> stateStack = new ArrayDeque<>();
		Deque<EtaSymbol> symbolStack = new ArrayDeque<>();
		Deque<Object> dataStack = new ArrayDeque<>();

		stateStack.push(states.get(0));

		while (tokenflow.hasNext()) {
			Token token = tokenflow.next();
			EtaParserState state = stateStack.peek();

			EtaParserAction action = state.getActions()[token.getTerminal().getId()];

			if (action == null) {
				throw new Exception("At line %s, column %s, unexpected token \"%s\" of type %s"
						.formatted(token.getLine(), token.getCol(), token.getData(), token.getTerminal()));
			}

			if (action.type() == EtaParserAction.SHIFT) {
				stateStack.push(states.get(action.target()));
				symbolStack.push(token.getTerminal());
				dataStack.push(token);
				continue;
			}

			if (action.type() == EtaParserAction.REDUCE) {
				Deque<Object> acc = new ArrayDeque<>();
				EtaNode current = action.rule().getReversedNodes().get(action.rule().getNodes().get(action.nodeIndex()))
						.get(0);

				EtaNode target = null;
				while ((target = current.getTransitions().get(symbolStack.peek())) != null
						&& stateStack.peek().getActiveRules().contains(action.rule())) {
					acc.push(dataStack.pop());
					symbolStack.pop();
					stateStack.pop();
					current = target;
				}
				tokenflow.pushback(token);
				stateStack.push(states.get(stateStack.peek().getGotos()[action.rule().getVariable().getId()]));
				symbolStack.push(action.rule().getVariable());
				dataStack.push(reducer.reduce(action.rule(), List.copyOf(acc)));
			}

			if (action.type() == EtaParserAction.ACCEPT) {
				assert dataStack.size() == 1;
				return dataStack.pop();
			}

		}

		throw new Exception("This should not happen");
	}

	public static record EtaParserAction(int type, int target, EtaRule rule, int nodeIndex) {

		public static final int ACCEPT = 0, SHIFT = 1, REDUCE = 2;

		@Override
		public String toString() {
			if (type == ACCEPT) {
				return "Action(Accept)";
			}
			if (type == SHIFT) {
				return "Action(Shift -> " + target + ")";
			}
			if (type == REDUCE) {
				return "Action(Reduce -> " + rule.getName() + ", " + nodeIndex + ")";
			}
			return null;
		}
	}

	public static class ParsedNode {

		private EtaRule rule;
		private List<Object> nodes;

		public ParsedNode(EtaRule rule, List<Object> nodes) {
			this.rule = rule;
			this.nodes = nodes;
		}

		public List<Object> getNodes() {
			return nodes;
		}

		@Override
		public String toString() {
			return "ParsedNode(" + rule.getName() + ")";
		}

		public static List<Object> getChildren(Object o) {
			if (o instanceof ParsedNode node) {
				return node.getNodes();
			}
			return List.of();
		}
	}

	@FunctionalInterface
	public static interface ParserReducer {

		public Object reduce(EtaRule rule, List<Object> items) throws Exception;

	}
}