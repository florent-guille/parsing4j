package org.parsing4j.etalg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Utils.Pair;
import org.parsing4j.etaengine.etaparser.EtaParser;
import org.parsing4j.etaengine.etaparser.EtaParserBuilder;
import org.parsing4j.etaengine.etaregex.EtaRegex;
import org.parsing4j.etaengine.etaregex.EtaRegexParser;
import org.parsing4j.tokenizer.regex.MatchingPolicy;
import org.parsing4j.tokenizer.regex.RegexTokenizer;
import org.parsing4j.tokenizer.regex.RegexTokenizerBuilder;
import org.parsing4j.tokenizer.regex.RegexTokenizerBuilder.RegexTokenizerBuilderEntry;
import org.parsing4j.tokenizer.regex.structure.Regex;
import org.parsing4j.tokenizer.regex.structure.RegexParser;

public class ETALG_Parser {

	public static EtaFrontend parseETALG(CharFlow flow) throws Exception {

		String startSymbol = null;

		Map<String, Pair<String, EtaRegex>> rawRules = new HashMap<>();
		Map<String, RegexTokenizerBuilderEntry> rawPatterns = new HashMap<>();

		while (flow.hasMore()) {
			String section = readSolidIdentifier(flow);
			if (Objects.equals(section, "startSymbol")) {
				startSymbol = readSolidIdentifier(flow);
				read(flow, ";");
				continue;
			}

			if (Objects.equals(section, "rules")) {
				read(flow, "{");

				while (flow.peek() != '}') {
					String name = readRuleName(flow);

					read(flow, ":");
					String symbol = readSolidIdentifier(flow);
					read(flow, "-->");

					EtaRegex regex = EtaRegexParser.readEtaRegex(flow);
					read(flow, ";");
					rawRules.put(name, new Pair<>(symbol, regex));
					continue;
				}

				read(flow, "}");
				continue;
			}

			if (Objects.equals(section, "patterns")) {
				read(flow, "{");

				while (flow.peek() != '}') {
					String name = readTerminalName(flow);
					read(flow, ":");

					MatchingPolicy policy = MatchingPolicy.GREEDY;

					if (flow.peek() != '"') {
						if (flow.peek() == 'n') {
							policy = MatchingPolicy.NO_MATCH;
							flow.next();
						} else if (flow.peek() == 'g') {
							policy = MatchingPolicy.GREEDY;
							flow.next();
						} else if (flow.peek() == 'r') {
							policy = MatchingPolicy.RELUCTANT;
							flow.next();
						} else {
							throw new Exception("Unknown matching policy: %s".formatted(flow.peek()));
						}
					}

					String rawRegex = readString(flow);

					Regex regex = RegexParser.readRegex(new CharFlow(rawRegex));

					Set<String> abovePatterns = new HashSet<>();

					if (flow.peek() == '>') {
						read(flow, ">");
						abovePatterns.add(readTerminalName(flow));

						while (flow.peek() == ',') {
							read(flow, ",");
							abovePatterns.add(readTerminalName(flow));
						}
					}

					read(flow, ";");
					rawPatterns.put(name, new RegexTokenizerBuilderEntry(regex, policy, abovePatterns));
				}

				read(flow, "}");
				continue;
			}

			throw new Exception("Unknown section found: %s".formatted(section));
		}

		EtaParserBuilder parserBuilder = new EtaParserBuilder();
		for (Entry<String, Pair<String, EtaRegex>> entry : rawRules.entrySet()) {
			parserBuilder.addRawRule(entry.getKey(), entry.getValue().left, entry.getValue().right);
		}

		parserBuilder.buildRules();
		parserBuilder.computeFirstSets();
		parserBuilder.computeStates(startSymbol);
		EtaParser parser = parserBuilder.createParser();

		RegexTokenizerBuilder tokenizerBuilder = new RegexTokenizerBuilder();
		for (Entry<String, RegexTokenizerBuilderEntry> entry : rawPatterns.entrySet()) {
			tokenizerBuilder.addPattern(parser.getTerminal(entry.getKey()), entry.getValue().regex(),
					entry.getValue().policy(), entry.getValue().abovePatterns());
		}

		RegexTokenizer tokenizer = tokenizerBuilder.build(parser.getEOF());

		return new EtaFrontend(tokenizer, parser);

	}

	private static String readSolidIdentifier(CharFlow flow) throws Exception {
		flow.skipBlanks();

		StringBuilder builder = new StringBuilder();
		while (Character.isJavaIdentifierPart(flow.peek())) {
			builder.append((char) flow.next());
		}

		flow.skipBlanks();
		if (builder.isEmpty()) {
			return null;
		}

		return builder.toString();
	}

	private static String readTerminalName(CharFlow flow) throws Exception {
		flow.skipBlanks();

		StringBuilder builder = new StringBuilder();
		flow.eat('\'');
		while (flow.peek() != '\'') {
			builder.append((char) flow.next());
		}
		flow.eat('\'');

		flow.skipBlanks();
		return builder.toString();
	}

	private static String readRuleName(CharFlow flow) throws Exception {
		flow.skipBlanks();

		StringBuilder builder = new StringBuilder();
		flow.eat('[');
		while (flow.peek() != ']') {
			builder.append((char) flow.next());
		}
		flow.eat(']');

		flow.skipBlanks();
		return builder.toString();
	}

	private static String readString(CharFlow flow) throws Exception {
		flow.skipBlanks();

		StringBuilder builder = new StringBuilder();
		flow.eat('"');
		boolean protect = false;
		while (flow.hasMore()) {
			if (protect) {
				builder.append(flow.next());
				protect = false;
				continue;
			}
			if (flow.peek() == '\\') {
				flow.eat('\\');
				protect = true;
				continue;
			}
			if (flow.peek() == '"') {
				break;
			}
			builder.append(flow.next());
		}
		flow.eat('"');

		flow.skipBlanks();
		return builder.toString();
	}

	private static void read(CharFlow flow, String target) throws Exception {
		flow.skipBlanks();

		for (int i = 0; i < target.length(); i++) {
			flow.eat(target.charAt(i));
		}

		flow.skipBlanks();
	}

}
