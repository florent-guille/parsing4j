package org.parsing4j.etaengine.etaregex;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.CharFlow.CharFlowException;

/**
 * The grammar for eta-regex is the following </br>
 * E -> T ('|' T)*</br>
 * T -> F* </br>
 * F -> B ('*'|'+'|'?')*</br>
 * B -> '(' E ')'</br>
 * B -> 'terminal'</br>
 * B -> 'non_terminal'
 **/
public class EtaRegexParser {

	public static EtaRegex readEtaRegex(CharFlow flow) throws IOException, CharFlowException {
		return readExpression(flow);
	}

	private static EtaRegex readExpression(CharFlow flow) throws IOException, CharFlowException {
		flow.skipBlanks();

		Deque<EtaRegex> stack = new ArrayDeque<>();
		stack.push(readTerm(flow));

		while (flow.hasMore() && flow.peek() == '|') {
			flow.eat('|');
			stack.push(readTerm(flow));
		}

		Deque<EtaRegex> choices = new ArrayDeque<>();
		for (EtaRegex regex : stack) {
			if (regex instanceof EtaRegexChoice) {
				for (EtaRegex child : regex.getChildren()) {
					choices.push(child);
				}
			} else {
				choices.push(regex);
			}
		}

		if (choices.size() == 1) {
			return choices.pop();
		}

		flow.skipBlanks();
		return new EtaRegexChoice(new ArrayList<>(choices));
	}

	private static EtaRegex readTerm(CharFlow flow) throws IOException, CharFlowException {
		flow.skipBlanks();

		Deque<EtaRegex> stack = new ArrayDeque<>();
		while (flow.hasMore() && flow.peek() != '|' && flow.peek() != ')' && flow.peek() != ';') {
			stack.push(readFactor(flow));
		}

		Deque<EtaRegex> sequence = new ArrayDeque<>();
		for (EtaRegex regex : stack) {
			if (regex instanceof EtaRegexSequence) {
				for (EtaRegex child : regex.getChildren()) {
					sequence.push(child);
				}
			} else {
				sequence.push(regex);
			}
		}

		if (sequence.size() == 0) {
			return EtaRegexBlank.BLANK;
		}

		if (sequence.size() == 1) {
			return sequence.pop();
		}

		flow.skipBlanks();
		return new EtaRegexSequence(new ArrayList<>(sequence));
	}

	private static EtaRegex readFactor(CharFlow flow) throws IOException, CharFlowException {
		flow.skipBlanks();

		EtaRegex result = readBase(flow);
		while (flow.hasMore() && (flow.peek() == '*' || flow.peek() == '+' || flow.peek() == '?')) {
			result = new EtaRegexQuantified(readQuantifier(flow), result);
		}

		flow.skipBlanks();
		return result;
	}

	private static int readQuantifier(CharFlow flow) throws IOException, CharFlowException {
		flow.skipBlanks();
		int quantifier = flow.next();
		flow.skipBlanks();
		return quantifier;
	}

	private static EtaRegex readBase(CharFlow flow) throws IOException, CharFlowException {
		flow.skipBlanks();
		EtaRegex result = null;

		block: {
			if (flow.peek() == '(') {
				flow.eat('(');
				result = readExpression(flow);
				flow.eat(')');
				break block;
			}

			if (flow.peek() == '\'') {
				result = readTerminal(flow);
				break block;
			}

			if (Character.isJavaIdentifierPart(flow.peek())) {
				result = readNonTerminal(flow);
				break block;
			}
		}

		flow.skipBlanks();
		return result;
	}

	private static EtaRegex readTerminal(CharFlow flow) throws IOException, CharFlowException {
		StringBuilder builder = new StringBuilder();

		flow.eat('\'');
		while (flow.peek() != '\'') {
			if (flow.peek() == '\\') {
				flow.eat('\\');
			}
			builder.append((char) flow.next());
		}
		flow.eat('\'');

		return new EtaTerminal(builder.toString());
	}

	private static EtaRegex readNonTerminal(CharFlow flow) throws IOException, CharFlowException {
		StringBuilder builder = new StringBuilder();

		while (flow.hasMore() && (Character.isJavaIdentifierPart(flow.peek()))) {
			builder.append((char) flow.next());
		}

		return new EtaNonTerminal(builder.toString());
	}

}