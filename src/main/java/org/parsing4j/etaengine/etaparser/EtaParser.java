package org.parsing4j.etaengine.etaparser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.parsing4j.core.PushbackIterator;
import org.parsing4j.core.Token;
import org.parsing4j.etaengine.etaregex.EtaTerminal;

public class EtaParser {

	private List<EtaParserState> states;
	private Map<String, EtaTerminal> terminalsMap;

	public EtaParser(List<EtaParserState> states, Map<String, EtaTerminal> terminalsMap) {
		this.states = states;
		this.terminalsMap = terminalsMap;
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

	public Object parse(Iterable<Token> iterable) throws Exception {
		return parse(iterable.iterator());
	}

	public Object parse(Iterator<Token> iterator) throws Exception {
		PushbackIterator<Token> tokenflow = new PushbackIterator<>(iterator);

		while (tokenflow.hasNext()) {
			Token current = tokenflow.next();
			System.out.println(current);
		}

		return null;
	}

	public static record EtaParserAction(int type, int target, EtaRule rule, int nodeIndex) {

		public static final int ACCEPT = 0, SHIFT = 1, REDUCE = 2;

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