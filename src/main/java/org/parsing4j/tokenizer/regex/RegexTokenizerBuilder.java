package org.parsing4j.tokenizer.regex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Utils.Pair;
import org.parsing4j.etaengine.etaregex.EtaTerminal;
import org.parsing4j.tokenizer.regex.structure.Regex;
import org.parsing4j.tokenizer.regex.structure.RegexBlank;
import org.parsing4j.tokenizer.regex.structure.RegexChoice;
import org.parsing4j.tokenizer.regex.structure.RegexClass;
import org.parsing4j.tokenizer.regex.structure.RegexParser;
import org.parsing4j.tokenizer.regex.structure.RegexQuantified;
import org.parsing4j.tokenizer.regex.structure.RegexRange;
import org.parsing4j.tokenizer.regex.structure.RegexSequence;

public class RegexTokenizerBuilder {

	private Map<EtaTerminal, RegexTokenizerBuilderEntry> patterns;

	public RegexTokenizerBuilder() {
		this.patterns = new HashMap<>();
	}

	public RegexTokenizerBuilder(Map<EtaTerminal, RegexTokenizerBuilderEntry> patterns) {
		this.patterns = patterns;
	}

	public void addPattern(EtaTerminal terminal, Regex regex, MatchingPolicy policy, Set<String> abovePatterns) {
		this.patterns.put(terminal, new RegexTokenizerBuilderEntry(regex, policy, abovePatterns));
	}

	public void addPattern(EtaTerminal terminal, String regex, MatchingPolicy policy, Set<String> abovePatterns)
			throws Exception {
		addPattern(terminal, RegexParser.readRegex(new CharFlow(regex)), policy, abovePatterns);
	}

	public RegexTokenizer build(EtaTerminal eof) throws Exception {
		Deque<RegexTokenizerBuildNode> buildNodes = new ArrayDeque<>();
		Supplier<RegexTokenizerBuildNode> nodeFactory = () -> {
			RegexTokenizerBuildNode result = new RegexTokenizerBuildNode(buildNodes.size());
			buildNodes.push(result);
			return result;
		};

		RegexTokenizerBuildNode buildRoot = nodeFactory.get();
		for (Entry<EtaTerminal, RegexTokenizerBuilderEntry> pattern : patterns.entrySet()) {
			if (pattern.getValue().policy == MatchingPolicy.NO_MATCH) {
				continue;
			}

			Deque<Regex> unfolded = pattern.getValue().regex.unfold();
			Deque<RegexTokenizerBuildNode> starts = new ArrayDeque<>();
			Deque<RegexTokenizerBuildNode> ends = new ArrayDeque<>();

			while (!unfolded.isEmpty()) {
				Regex current = unfolded.pop();

				if (current instanceof RegexClass c) {
					RegexTokenizerBuildNode start = nodeFactory.get();
					RegexTokenizerBuildNode end = nodeFactory.get();

					for (RegexRange range : c.getRanges()) {
						start.addTransition(range, end);
					}
					starts.push(start);
					ends.push(end);
					continue;
				}

				if (current instanceof RegexBlank) {
					RegexTokenizerBuildNode node = nodeFactory.get();
					starts.push(node);
					ends.push(node);
					continue;
				}

				if (current instanceof RegexChoice) {
					RegexTokenizerBuildNode commonStart = nodeFactory.get();
					RegexTokenizerBuildNode commonEnd = nodeFactory.get();

					List<Regex> children = current.getChildren();
					for (int i = 0; i < children.size(); i++) {
						RegexTokenizerBuildNode start = starts.pop();
						RegexTokenizerBuildNode end = ends.pop();
						commonStart.addEpsilonTransition(start);
						end.addEpsilonTransition(commonEnd);
					}

					starts.push(commonStart);
					ends.push(commonEnd);
					continue;
				}

				if (current instanceof RegexSequence) {
					RegexTokenizerBuildNode commonEnd = nodeFactory.get();
					RegexTokenizerBuildNode previousStart = commonEnd;

					List<Regex> children = current.getChildren();
					for (int i = 0; i < children.size(); i++) {
						RegexTokenizerBuildNode start = starts.pop();
						RegexTokenizerBuildNode end = ends.pop();
						end.addEpsilonTransition(previousStart);
						previousStart = start;
					}

					starts.push(previousStart);
					ends.push(commonEnd);
					continue;
				}

				if (current instanceof RegexQuantified q) {
					RegexTokenizerBuildNode start = starts.pop();
					RegexTokenizerBuildNode end = ends.pop();

					if (q.getQuantifier() == '?') {
						start.addEpsilonTransition(end);
					}

					if (q.getQuantifier() == '*') {
						start.addEpsilonTransition(end);
						end.addEpsilonTransition(start);
					}

					if (q.getQuantifier() == '+') {
						end.addEpsilonTransition(start);
					}

					starts.push(start);
					ends.push(end);
					continue;
				}
			}

			RegexTokenizerBuildNode start = starts.pop();
			RegexTokenizerBuildNode end = ends.pop();
			assert starts.isEmpty();
			assert ends.isEmpty();

			buildRoot.addEpsilonTransition(start);
			end.setPattern(new Pair<>(pattern.getValue().policy, pattern.getKey()));
		}

		List<RegexTokenizerNode> nodes = RegexTokenizerBuildNode.determinize(buildRoot,
				new ArrayList<>(buildNodes.reversed()));

		return new RegexTokenizer(nodes, eof);
	}

	public record RegexTokenizerBuilderEntry(Regex regex, MatchingPolicy policy, Set<String> abovePatterns) {

	}
}