package org.parsing4j.etaengine.regex;

import static org.junit.Assert.assertEquals;

import static org.parsing4j.etaengine.regex.EtaRegexBuilder.blank;
import static org.parsing4j.etaengine.regex.EtaRegexBuilder.choice;
import static org.parsing4j.etaengine.regex.EtaRegexBuilder.plus;
import static org.parsing4j.etaengine.regex.EtaRegexBuilder.qmark;
import static org.parsing4j.etaengine.regex.EtaRegexBuilder.seq;
import static org.parsing4j.etaengine.regex.EtaRegexBuilder.star;
import static org.parsing4j.etaengine.regex.EtaRegexBuilder.term;

import org.junit.Test;
import org.parsing4j.core.CharFlow;

/*
 * @author Florent Guille
 * */
public class EtaRegexParserTest {

	@Test
	public void test$1() throws Exception {
		EtaRegex obtained = EtaRegexParser.parseEtaRegex(new CharFlow("('A' 'B')*"));
		EtaRegex expected = star(seq(term("A"), term("B")));

		assertEquals(expected, obtained);
	}

	@Test
	public void test$2() throws Exception {
		EtaRegex obtained = EtaRegexParser.parseEtaRegex(new CharFlow("'A'|| 'B' * 'C' ('D'? | 'E'+)"));
		EtaRegex expected = choice(term("A"), blank(),
				seq(star(term("B")), term("C"), choice(qmark(term("D")), plus(term("E")))));

		assertEquals(expected, obtained);
	}
}