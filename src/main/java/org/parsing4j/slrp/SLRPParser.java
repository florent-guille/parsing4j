package org.parsing4j.slrp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.CharFlow.CharFlowException;
import org.parsing4j.core.CharFlow.UnwantedCharException;
import org.parsing4j.core.Tag;
import org.parsing4j.core.TagElement;
import org.parsing4j.core.TagElementArray;
import org.parsing4j.core.TagElementBoolean;
import org.parsing4j.core.TagElementInteger;
import org.parsing4j.core.TagElementObject;
import org.parsing4j.core.TagElementString;
import org.parsing4j.core.Utils;
import org.parsing4j.core.Utils.Triplet;
import org.parsing4j.etaengine.parser.EtaParserBuilder.EtaParserBuilderEntry;
import org.parsing4j.etaengine.regex.EtaRegex;
import org.parsing4j.etaengine.regex.EtaRegexParser;
import org.parsing4j.tokenizer.regex.MatchingPolicy;
import org.parsing4j.tokenizer.regex.Regex;
import org.parsing4j.tokenizer.regex.RegexParser;
import org.parsing4j.tokenizer.regex.RegexTokenizerBuilder.RegexTokenizerBuilderEntry;

/*
 * @author Florent Guille
 * */
public class SLRPParser {

	public static Triplet<String, List<EtaParserBuilderEntry>, List<RegexTokenizerBuilderEntry>> parseSLRP(
			InputStream stream) throws IOException, CharFlowException {
		return parseSLRP(new CharFlow(stream));
	}

	public static Triplet<String, List<EtaParserBuilderEntry>, List<RegexTokenizerBuilderEntry>> parseSLRP(
			CharFlow flow) throws IOException, CharFlowException {
		String startSymbol = null;
		List<EtaParserBuilderEntry> parserEntries = new ArrayList<>();
		List<RegexTokenizerBuilderEntry> tokenizerEntries = new ArrayList<>();

		flow.skipBlanks();

		while (flow.hasMore()) {

			String identifier = readSolidIdentifier(flow);

			if (Objects.equals(identifier, "startSymbol")) {
				startSymbol = readSolidIdentifier(flow);
				read(flow, ";");
				continue;
			}

			if (Objects.equals(identifier, "rules")) {

				read(flow, "{");

				while (flow.peek() != '}') {
					parserEntries.add(readRule(flow));
				}

				read(flow, "}");

				continue;
			}

			if (Objects.equals(identifier, "patterns")) {

				read(flow, "{");

				while (flow.peek() != '}') {
					tokenizerEntries.add(readPattern(flow));
				}

				read(flow, "}");

				continue;
			}
		}

		return new Triplet<>(startSymbol, parserEntries, tokenizerEntries);
	}

	public static String readSolidIdentifier(CharFlow flow) throws IOException, CharFlowException {
		if (!Character.isJavaIdentifierStart(flow.peek())) {
			throw new UnwantedCharException(flow.getLine(), flow.getColumn(), flow.peek());
		}

		StringBuilder builder = new StringBuilder();

		builder.append((char) flow.next());
		while (flow.hasMore() && Character.isJavaIdentifierPart(flow.peek())) {
			builder.append((char) flow.next());
		}

		flow.skipBlanks();

		return builder.toString();
	}

	private static Tag readTag(CharFlow flow) throws IOException, CharFlowException {
		flow.eat('@');
		String name = readSolidIdentifier(flow);

		List<TagElement> elements = new ArrayList<>();

		if (flow.peek() == '(') {
			read(flow, "(");

			boolean next = flow.peek() != ')';
			while (next) {
				next = false;

				elements.add(readTagElement(flow));

				if (flow.peek() == ',') {
					read(flow, ",");
					next = true;
				}
			}

			read(flow, ")");
		}

		return new Tag(name, elements);

	}

	private static TagElement readTagElement(CharFlow flow) throws IOException, CharFlowException {
		if (flow.peek() == 't') {
			read(flow, "true");
			return new TagElementBoolean(true);
		}

		if (flow.peek() == 'f') {
			read(flow, "false");
			return new TagElementBoolean(false);
		}

		if (flow.peek() == '"') {
			return new TagElementString(readString(flow));

		}

		if (Character.isDigit(flow.peek())) {
			return new TagElementInteger(readInteger(flow));
		}

		if (flow.peek() == '{') {
			return readTagObject(flow);
		}

		if (flow.peek() == '[') {
			return readTagArray(flow);
		}

		throw new UnwantedCharException(flow.getLine(), flow.getColumn(), flow.peek());
	}

	private static TagElement readTagObject(CharFlow flow) throws IOException, CharFlowException {
		read(flow, "{");

		Map<String, TagElement> data = new HashMap<>();

		boolean next = flow.peek() != '}';
		while (next) {
			next = false;

			String name = readString(flow);
			read(flow, ":");
			TagElement value = readTagElement(flow);

			data.put(name, value);

			if (flow.peek() == ',') {
				read(flow, ",");
				next = true;
			}
		}

		read(flow, "}");

		return new TagElementObject(data);
	}

	private static int readInteger(CharFlow flow) throws IOException, CharFlowException {
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

		int result = 0;
		int current = 0;
		while ((current = Utils.getDigitValue((char) flow.peek(), base)) >= 0) {
			flow.next();
			result = 10 * result + current;
		}

		return result;
	}

	private static TagElementArray readTagArray(CharFlow flow) throws IOException, CharFlowException {
		read(flow, "[");

		List<TagElement> values = new ArrayList<>();

		boolean next = flow.peek() != ']';

		while (next) {
			next = false;
			values.add(readTagElement(flow));

			if (flow.peek() == ',') {
				read(flow, ",");
				next = true;
			}
		}

		read(flow, "]");

		return new TagElementArray(values);
	}

	private static String readString(CharFlow flow) throws IOException, CharFlowException {
		flow.eat('"');

		StringBuilder builder = new StringBuilder();
		while (flow.peek() != '"') {
			if (flow.peek() == '\\') {
				flow.eat('\\');

				if (flow.peek() == 'n') {
					flow.eat('n');
					builder.append((char) 10);
					continue;
				}

				builder.append((char) flow.next());
				continue;
			}

			builder.append((char) flow.next());
		}
		flow.eat('"');
		flow.skipBlanks();

		return builder.toString();
	}

	public static EtaParserBuilderEntry readRule(CharFlow flow) throws IOException, CharFlowException {
		List<Tag> tags = new ArrayList<>();
		while (flow.peek() == '@') {
			tags.add(readTag(flow));
		}
		String name = null;

		if (flow.peek() == '[') {
			name = readRuleName(flow);
			read(flow, ":");
		}

		String var = readSolidIdentifier(flow);

		read(flow, "-->");

		EtaRegex regex = EtaRegexParser.parseEtaRegex(flow);

		read(flow, ";");

		return new EtaParserBuilderEntry(name, var, regex, tags);
	}

	public static String readRuleName(CharFlow flow) throws IOException, CharFlowException {
		StringBuilder builder = new StringBuilder();
		flow.eat('[');
		while (flow.peek() != ']') {
			builder.append((char) flow.next());
		}
		flow.eat(']');

		flow.skipBlanks();

		return builder.toString();
	}

	private static RegexTokenizerBuilderEntry readPattern(CharFlow flow) throws IOException, CharFlowException {
		Set<String> tags = new HashSet<>();
		while (flow.peek() == '@') {
			flow.eat('@');
			tags.add(readSolidIdentifier(flow));
		}

		String name = readPatternName(flow);

		read(flow, "<--");

		MatchingPolicy policy = MatchingPolicy.GREEDY;

		if (flow.peek() == 'n') {
			policy = MatchingPolicy.NO_MATCH;
			flow.eat('n');
		} else if (flow.peek() == 'g') {
			policy = MatchingPolicy.GREEDY;
			flow.eat('g');
		} else if (flow.peek() == 'r') {
			policy = MatchingPolicy.RELUCTANT;
			flow.eat('r');
		}

		Regex regex = RegexParser.readRegex(new CharFlow(readString(flow)));

		read(flow, ";");

		return new RegexTokenizerBuilderEntry(name, regex, policy, Set.of(), tags);
	}

	public static String readPatternName(CharFlow flow) throws IOException, CharFlowException {
		StringBuilder builder = new StringBuilder();
		flow.eat('\'');
		while (flow.peek() != '\'') {
			builder.append((char) flow.next());
		}
		flow.eat('\'');

		flow.skipBlanks();

		return builder.toString();
	}

	public static void read(CharFlow flow, String target) throws IOException, CharFlowException {
		for (int i = 0; i < target.length(); i++) {
			flow.eat(target.charAt(i));
		}
		flow.skipBlanks();
	}

}
