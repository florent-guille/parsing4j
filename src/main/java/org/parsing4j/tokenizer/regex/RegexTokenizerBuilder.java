package org.parsing4j.tokenizer.regex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Utils.Pair;
import org.parsing4j.etaengine.regex.EtaSolidTerminal;

/*
 * @author Florent Guille
 * */
public class RegexTokenizerBuilder {

	private List<RegexTokenizerBuilderEntry> entries;;

	public RegexTokenizerBuilder() {
		this.entries = new ArrayList<>();
	}

	public void addEntry(RegexTokenizerBuilderEntry entry) {
		this.entries.add(entry);
	}

	public void addPattern(String terminal, Regex regex, MatchingPolicy policy, Set<String> abovePatterns) {
		this.entries.add(new RegexTokenizerBuilderEntry(terminal, regex, policy, abovePatterns, Set.of()));
	}

	public void addPattern(String terminal, String regex, MatchingPolicy policy, Set<String> abovePatterns)
			throws Exception {
		addPattern(terminal, RegexParser.readRegex(new CharFlow(regex)), policy, abovePatterns);
	}

	public RegexTokenizer build(Function<String, EtaSolidTerminal> terminalProvider) throws Exception {
		Deque<RegexTokenizerBuildNode> buildNodes = new ArrayDeque<>();
		Supplier<RegexTokenizerBuildNode> nodeFactory = () -> {
			RegexTokenizerBuildNode result = new RegexTokenizerBuildNode(buildNodes.size());
			buildNodes.push(result);
			return result;
		};

		RegexTokenizerBuildNode buildRoot = nodeFactory.get();
		for (RegexTokenizerBuilderEntry entry : entries) {
			if (entry.policy() == MatchingPolicy.NO_MATCH) {
				continue;
			}

			Deque<Regex> unfolded = entry.regex().unfold();
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
			end.setPattern(new Pair<>(entry.policy(), terminalProvider.apply(entry.terminal())));

			end.getPattern().right.setTags(entry.tags);
		}

		List<RegexTokenizerNode> nodes = RegexTokenizerBuildNode.determinize(buildRoot,
				new ArrayList<>(buildNodes.reversed()));

		return new RegexTokenizer(nodes);
	}

	public record RegexTokenizerBuilderEntry(String terminal, Regex regex, MatchingPolicy policy,
			Set<String> abovePatterns, Set<String> tags) {

	}
}