package org.parsing4j.etaengine.etaregex;

import java.util.List;

import org.junit.Test;
import org.parsing4j.core.Token;
import org.parsing4j.core.Utils;
import org.parsing4j.etaengine.etaparser.EtaParser;
import org.parsing4j.etaengine.etaparser.EtaParser.ParsedNode;
import org.parsing4j.etaengine.etaparser.EtaParserBuilder;

public class EtaRegexParserTest {

	@Test
	public void parserTest$1() throws Exception {
		EtaParserBuilder builder = new EtaParserBuilder();
		builder.addRawRule("main", "S", "E");
		builder.addRawRule("sum", "E", "T ('+' T)*");
		builder.addRawRule("product", "T", "F ('*' F)*");
		builder.addRawRule("parenthesis", "F", "'(' E ')'");
		builder.addRawRule("var", "F", "'id'");
		builder.buildRules();
		builder.computeFirstSets();
		builder.computeStates("S");
		EtaParser parser = builder.createParser();

		List<Token> tokens = List.of(//
				new Token(parser.getTerminal("id"), "var_a", 0, 0), //
				new Token(parser.getTerminal("+"), "+", 0, 2), //
				new Token(parser.getTerminal("id"), "c", 0, 3), //
				new Token(parser.getEOF(), null, 0, 4)//
		);
		Object result = parser.parse(tokens);

		System.out.println(Utils.treeRepr(result, Object::toString, ParsedNode::getChildren));
	}

}