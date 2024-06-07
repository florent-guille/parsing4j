package org.parsing4j.tokenizer.regex;

import java.util.Objects;
import java.util.Set;

import org.junit.Test;
import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Utils;
import org.parsing4j.etaengine.etaparser.EtaParser;
import org.parsing4j.etaengine.etaparser.EtaParser.ParsedNode;
import org.parsing4j.etaengine.etaparser.EtaParserBuilder;

public class RegexTokenizerBuilderTest {

	@Test
	public void buildTest$1() throws Exception {
		EtaParserBuilder parserBuilder = new EtaParserBuilder();
		parserBuilder.addRawRule("main", "S", "E");
		parserBuilder.addRawRule("sum", "E", "T ('+' T)*");
		parserBuilder.addRawRule("product", "T", "F ('*' F)*");
		parserBuilder.addRawRule("parenthesis", "F", "'(' E ')'");
		parserBuilder.addRawRule("var", "F", "'id'");

		EtaParser parser = parserBuilder.build("S");

		RegexTokenizerBuilder builder = new RegexTokenizerBuilder();
		builder.addPattern(parser.getTerminal("id"), "[a-zA-Z][a-zA-Z0-9]*", MatchingPolicy.GREEDY, Set.of());
		builder.addPattern(parser.getTerminal("blank"), "{Blank}+", MatchingPolicy.GREEDY, Set.of());
		builder.addPattern(parser.getTerminal("+"), "'+'", MatchingPolicy.RELUCTANT, Set.of());
		builder.addPattern(parser.getTerminal("("), "'('", MatchingPolicy.RELUCTANT, Set.of());
		builder.addPattern(parser.getTerminal(")"), "')'", MatchingPolicy.RELUCTANT, Set.of());
		builder.addPattern(parser.getTerminal("*"), "'*'", MatchingPolicy.RELUCTANT, Set.of());

		RegexTokenizer tokenizer = builder.build(parser.getEOF());

		CharFlow flow = new CharFlow("a + b * (c + d)");

		Object result = parser.parse(tokenizer.tokenizeFlow(flow));

		System.out.println(Utils.treeRepr(result, Objects::toString, ParsedNode::getChildren));
	}

}
