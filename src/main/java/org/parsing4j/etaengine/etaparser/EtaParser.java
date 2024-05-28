package org.parsing4j.etaengine.etaparser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.parsing4j.core.PushbackIterator;
import org.parsing4j.core.Token;
import org.parsing4j.etaengine.etaregex.EtaSymbol;
import org.parsing4j.etaengine.etaregex.EtaTerminal;

public class EtaParser {

	private List<EtaParserState> states;
	private Map<String, EtaTerminal> terminalsMap;
	private EtaTerminal eof;
	private BiFunction<? super EtaRule, ? super List<Object>, ?> reducer;

	public EtaParser(List<EtaParserState> states, Map<String, EtaTerminal> terminalsMap, EtaTerminal eof) {
		this.states = states;
		this.terminalsMap = terminalsMap;
		this.eof = eof;
	}

	public EtaTerminal getTerminal(String name) {
		if (!terminalsMap.containsKey(name)) {
			terminalsMap.put(name, new EtaTerminal(name));
		}
		return terminalsMap.get(name);
	}

	public List<EtaParserState> getStates() {
		return states;
	}

	public void setReducer(BiFunction<? super EtaRule, ? super List<Object>, ?> reducer) {
		this.reducer = reducer;
	}

	public EtaTerminal getEOF() {
		return eof;
	}

	public Object parse(Iterable<Token> iterable) throws Exception {
		return parse(iterable.iterator());
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
				dataStack.push(List.copyOf(acc));
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
}