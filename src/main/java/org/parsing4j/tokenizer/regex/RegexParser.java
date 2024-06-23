package org.parsing4j.tokenizer.regex;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.CharFlow.CharFlowException;
import org.parsing4j.core.Utils;

/**
 * Grammar for regular expressions <br>
 * E -> T ('|' T)*<br>
 * T -> F* <br>
 * F -> B ('*'|'+'|'?')* <br>
 * B -> '(' E ')' <br>
 * B -> char <br>
 * B -> 'litteral' <br>
 * B -> 'fragment' <br>
 * B -> '[' I? R* (',' char*)?]<br>
 * R -> char ('-' char)? <br>
 * I -> '^'
 * 
 * @author Florent Guille
 */
public class RegexParser {

	public static final Map<String, Regex> POSIX_CLASSES = Map
			.ofEntries(
					Map.entry("Blank",
							new RegexClass(List.of(new RegexRange(0x9, 0xD), new RegexRange(0x20, 0x20),
									new RegexRange(0x85), new RegexRange(0xA0)))),
					Map.entry("Any", new RegexClass(List.of(new RegexRange(0, 255)))));

	public static Regex readRegex(CharFlow flow) throws IOException, CharFlowException {
		Deque<Regex> choices = new ArrayDeque<>();
		choices.push(readTerm(flow));

		while (flow.hasMore() && flow.peek() == '|') {
			flow.eat('|');
			choices.push(readTerm(flow));
		}

		Deque<Regex> result = new ArrayDeque<>();
		for (Regex item : choices) {
			if (item instanceof RegexChoice choice) {
				for (Regex child : choice.getChildren()) {
					result.push(child);
				}
			} else {
				result.push(item);
			}
		}

		if (result.size() == 1) {
			return result.pop();
		}

		return new RegexChoice(new ArrayList<>(result));
	}

	private static Regex readTerm(CharFlow flow) throws IOException, CharFlowException {
		Deque<Regex> sequence = new ArrayDeque<>();

		while (flow.hasMore() && flow.peek() != ')' && flow.peek() != '|') {
			sequence.push(readFactor(flow));
		}

		Deque<Regex> result = new ArrayDeque<>();
		for (Regex item : sequence) {
			if (item instanceof RegexSequence seq) {
				for (Regex child : seq.getChildren()) {
					result.push(child);
				}
			} else {
				result.push(item);
			}
		}

		if (result.size() == 0) {
			return new RegexBlank();
		}

		if (result.size() == 1) {
			return result.pop();
		}

		return new RegexSequence(new ArrayList<>(result));
	}

	private static Regex readFactor(CharFlow flow) throws IOException, CharFlowException {
		Regex result = readBase(flow);

		while (flow.hasMore() && (flow.peek() == '*' || flow.peek() == '+' || flow.peek() == '?')) {
			result = new RegexQuantified(flow.next(), result);
		}
		return result;
	}

	private static Regex readBase(CharFlow flow) throws IOException, CharFlowException {
		if (flow.peek() == '(') {
			flow.eat('(');
			Regex result = readRegex(flow);
			flow.eat(')');
			return result;
		}

		if (flow.peek() == '\'') {
			return readLitteral(flow);
		}

		if (flow.peek() == '<') {
			flow.eat('<');
			String name = readLitteralName(flow);
			flow.eat('>');
			return new RegexFragment(name);
		}

		if (flow.peek() == '[') {
			flow.eat('[');
			boolean inverted = false;
			int min = 0, max = 255;

			if (flow.peek() == '^') {
				flow.eat('^');
				inverted = true;
			}
			Deque<RegexRange> ranges = new ArrayDeque<>();
			while (flow.peek() != ']' && flow.peek() != ',') {
				int start = readChar(flow);
				int end = start;
				if (flow.peek() == '-') {
					flow.eat('-');
					end = readChar(flow);
				}
				ranges.push(new RegexRange(start, end));
			}

			if (flow.peek() == ',') {
				flow.eat(',');
				while (flow.peek() != ']') {
					ranges.push(new RegexRange(readChar(flow)));
				}
			}

			flow.eat(']');

			List<RegexRange> result = RegexRange.segment(new ArrayList<>(ranges));
			if (inverted) {
				result = RegexRange.invert(result, min, max);
			}

			return new RegexClass(result);
		}

		if (flow.peek() == '{') {
			String name = readPosixName(flow);
			if (!POSIX_CLASSES.containsKey(name)) {
				throw new IOException("Unknown posix class: %s".formatted(name));
			}

			return POSIX_CLASSES.get(name);
		}

		return new RegexClass(List.of(new RegexRange(readChar(flow))));
	}

	private static int readChar(CharFlow flow) throws IOException, CharFlowException {
		if (flow.peek() == '\\') {
			flow.eat('\\');

			if (flow.peek() == 'u') {
				flow.eat('u');
				return readNumber(flow);
			}

			if (flow.peek() == 'n') {
				flow.eat('n');
				return 10;
			}

			if (flow.peek() == 't') {
				flow.eat('t');
				return 9;
			}

			return flow.next();
		}
		return flow.next();
	}

	private static Regex readLitteral(CharFlow flow) throws IOException, CharFlowException {
		String result = readLitteralName(flow);
		List<Regex> ranges = new ArrayList<>(result.length());
		for (int i = result.length() - 1; i >= 0; i--) {
			ranges.add(new RegexClass(List.of(new RegexRange(result.charAt(i)))));
		}

		return new RegexSequence(ranges);
	}

	private static String readLitteralName(CharFlow flow) throws IOException, CharFlowException {
		flow.eat('\'');
		StringBuilder builder = new StringBuilder();

		while (flow.peek() != '\'') {
			if (flow.peek() == '\\') {
				flow.eat('\\');
				if (flow.peek() == '\'') {
					flow.eat('\'');
					builder.append("'");
					continue;
				}
				builder.append('\\');
				continue;
			}
			builder.append((char) flow.next());
		}
		flow.eat('\'');
		return builder.toString();
	}

	private static String readPosixName(CharFlow flow) throws IOException, CharFlowException {
		flow.eat('{');
		StringBuilder builder = new StringBuilder();

		while (flow.peek() != '}') {
			builder.append((char) flow.next());
		}
		flow.eat('}');
		return builder.toString();
	}

	private static int readNumber(CharFlow flow) throws IOException, CharFlowException {
		int code = 0;
		int base = 10;

		if (flow.peek() == '0') {
			flow.eat('0');

			if (flow.peek() == 'x') {
				flow.eat('x');
				base = 16;
			} else if (flow.peek() == 'b') {
				flow.eat('b');
				base = 2;
			}

		}
		int value = -1;
		while (flow.hasMore() && ((value = Utils.getDigitValue((char) flow.peek(), base)) >= 0 && value <= base)) {
			code = base * code + Character.digit(flow.next(), base);
		}

		return code;
	}
}