package org.parsing4j.tokenizer.regex;

import org.junit.Test;
import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Utils;

public class RegexParserTest {

	@Test
	public void regexParsingTest$1() throws Exception{
		String input = "[,\\u0xA]*'test'<'unit'>";
		Regex regex = RegexParser.readRegex(new CharFlow(input));
		System.out.println(Utils.treeRepr(regex, Regex::getPrettyRepr, Regex::getChildren));
	}
}
