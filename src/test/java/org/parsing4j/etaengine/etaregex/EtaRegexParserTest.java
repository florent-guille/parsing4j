package org.parsing4j.etaengine.etaregex;

import org.junit.Test;
import org.parsing4j.etaengine.etaparser.EtaParser;
import org.parsing4j.etaengine.etaparser.EtaParserBuilder;

public class EtaRegexParserTest {

	@Test
	public void parserTest$Test1() throws Exception {
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
	}

}