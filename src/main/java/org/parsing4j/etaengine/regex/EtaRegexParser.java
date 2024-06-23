package org.parsing4j.etaengine.regex;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.CharFlow.CharFlowException;
import org.parsing4j.core.CharFlow.UnwantedCharException;
import org.parsing4j.etaengine.regex.EtaQuantified.EtaQuantifier;

/**
 * Parser for EtaRegex. The grammar used is the following: <br>
 * E -> T ('|' T)* <br>
 * T -> F* <br>
 * F -> B ('*'|'+'|'?')* <br>
 * B -> '(' E ')' <br>
 * B -> symbol <br>
 * symbol -> 'terminal' <br>
 * symbol -> 'variable' <br>
 * 
 * @author Florent Guille
 */
public class EtaRegexParser {

	public static EtaRegex parseEtaRegex(CharFlow flow) throws IOException, CharFlowException {
		flow.skipBlanks();
		return readRegex(flow);
	}

	private static EtaRegex readRegex(CharFlow flow) throws IOException, CharFlowException {
		Deque<EtaRegex> items = new ArrayDeque<>();
		items.push(readTerm(flow));

		while (flow.hasMore() && flow.peek() == '|') {
			read(flow, '|');
			items.push(readTerm(flow));
		}

		Deque<EtaRegex> result = new ArrayDeque<>();
		for (EtaRegex regex : items) {
			if (regex instanceof EtaChoice) {
				for (EtaRegex child : regex.getChildren()) {
					result.push(child);
				}
			} else {
				result.push(regex);
			}
		}

		if (result.size() == 1) {
			return result.pop();
		}

		return new EtaChoice(new ArrayList<>(result));
	}

	private static EtaRegex readTerm(CharFlow flow) throws IOException, CharFlowException {
		Deque<EtaRegex> items = new ArrayDeque<>();
		while (flow.hasMore() && flow.peek() != '|' && flow.peek() != ')' && flow.peek() != ';') {
			items.push(readFactor(flow));
		}

		Deque<EtaRegex> result = new ArrayDeque<>();
		for (EtaRegex regex : items) {
			if (regex instanceof EtaSequence) {
				for (EtaRegex child : regex.getChildren()) {
					result.push(child);
				}
			} else {
				result.push(regex);
			}
		}

		if (result.size() == 0) {
			return new EtaBlank();
		}

		if (result.size() == 1) {
			return result.pop();
		}

		return new EtaSequence(new ArrayList<>(result));
	}

	private static EtaRegex readFactor(CharFlow flow) throws IOException, CharFlowException {
		EtaRegex result = readBase(flow);

		while (flow.hasMore() && (flow.peek() == '*' || flow.peek() == '+' || flow.peek() == '?')) {
			result = new EtaQuantified(EtaQuantifier.getQuantifier(next(flow)), result);
		}

		return result;
	}

	private static EtaRegex readBase(CharFlow flow) throws IOException, CharFlowException {

		if (flow.peek() == '(') {
			read(flow, '(');
			EtaRegex result = readRegex(flow);
			read(flow, ')');

			return result;
		}

		if (flow.peek() == '\'') {
			return readTerminal(flow);
		}
		
		if(Character.isJavaIdentifierPart(flow.peek())) {
			return readVariable(flow);
		}

		throw new UnwantedCharException(flow.getLine(), flow.getColumn(), flow.peek());
	}

	private static EtaSolidTerminal readTerminal(CharFlow flow) throws IOException, CharFlowException {
		flow.eat('\'');
		StringBuilder builder = new StringBuilder();

		boolean protect = false;
		while (flow.hasMore()) {
			if (protect) {
				protect = false;
				builder.append((char) flow.next());
				continue;
			}

			if (flow.peek() == '\\') {
				protect = true;
				continue;
			}

			if (flow.peek() == '\'') {
				break;
			}

			builder.append((char) flow.next());
		}
		flow.eat('\'');

		flow.skipBlanks();

		return new EtaSolidTerminal(builder.toString());
	}

	private static EtaVariable readVariable(CharFlow flow) throws IOException, CharFlowException {
		StringBuilder builder = new StringBuilder();
		if (!Character.isJavaIdentifierStart(flow.peek())) {
			throw new UnwantedCharException(flow.getLine(), flow.getColumn(), flow.peek());
		}

		builder.append((char) flow.next());

		while (flow.hasMore() && Character.isJavaIdentifierPart(flow.peek())) {
			builder.append((char) flow.next());
		}
		
		flow.skipBlanks();

		return new EtaVariable(builder.toString());
	}

	private static void read(CharFlow flow, int target) throws IOException, CharFlowException {
		flow.eat(target);
		flow.skipBlanks();
	}

	private static int next(CharFlow flow) throws IOException, CharFlowException {
		int result = flow.next();
		flow.skipBlanks();
		return result;
	}

}
