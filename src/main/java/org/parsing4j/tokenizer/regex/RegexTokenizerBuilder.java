package org.parsing4j.tokenizer.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.parsing4j.core.CharFlow;

public class RegexTokenizerBuilder {

	private Map<String, RegexTokenizerBuilderEntry> rawPatterns;

	public RegexTokenizerBuilder() {
		this.rawPatterns = new HashMap<>();
	}

	public void addPattern(String name, Regex regex, MatchingPolicy policy, Set<String> abovePatterns) {
		this.rawPatterns.put(name, new RegexTokenizerBuilderEntry(regex, policy, abovePatterns));
	}

	public void addPattern(String name, String regex, MatchingPolicy policy, Set<String> abovePatterns)
			throws Exception {
		addPattern(name, RegexParser.readRegex(new CharFlow(regex)), policy, abovePatterns);
	}

	private record RegexTokenizerBuilderEntry(Regex regex, MatchingPolicy policy, Set<String> abovePatterns) {

	}
}