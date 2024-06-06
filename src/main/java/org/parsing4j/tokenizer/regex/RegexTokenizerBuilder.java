package org.parsing4j.tokenizer.regex;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.parsing4j.core.CharFlow;
import org.parsing4j.etaengine.etaregex.EtaTerminal;
import org.parsing4j.tokenizer.regex.structure.Regex;
import org.parsing4j.tokenizer.regex.structure.RegexParser;

public class RegexTokenizerBuilder {

	private Map<EtaTerminal, RegexTokenizerBuilderEntry> patterns;

	public RegexTokenizerBuilder() {
		this.patterns = new HashMap<>();
	}

	public void addPattern(EtaTerminal terminal, Regex regex, MatchingPolicy policy, Set<String> abovePatterns) {
		this.patterns.put(terminal, new RegexTokenizerBuilderEntry(regex, policy, abovePatterns));
	}

	public void addPattern(EtaTerminal terminal, String regex, MatchingPolicy policy, Set<String> abovePatterns)
			throws Exception {
		addPattern(terminal, RegexParser.readRegex(new CharFlow(regex)), policy, abovePatterns);
	}

	public void build() {
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
			
			List<Regex> unfolded = pattern.getValue().regex.unfold();
			System.out.println(unfolded);
		}
	}

	private record RegexTokenizerBuilderEntry(Regex regex, MatchingPolicy policy, Set<String> abovePatterns) {

	}
}