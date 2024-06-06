package org.parsing4j.tokenizer.regex;

import java.util.Set;

import org.junit.Test;
import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Token;
import org.parsing4j.etaengine.etaregex.EtaTerminal;

public class RegexTokenizerBuilderTest {

	@Test
	public void buildTest$1() throws Exception {
		RegexTokenizerBuilder builder = new RegexTokenizerBuilder();
		builder.addPattern(new EtaTerminal("id"), "[a-zA-Z][a-zA-Z0-9]*", MatchingPolicy.GREEDY, Set.of());

		RegexTokenizer tokenizer = builder.build();

		CharFlow flow = new CharFlow("test  ");
		Token result = tokenizer.nextToken(flow);
		System.out.println(result);
		result = tokenizer.nextToken(flow);
	}

}
