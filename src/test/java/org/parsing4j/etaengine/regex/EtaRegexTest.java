package org.parsing4j.etaengine.regex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.junit.Test;
import org.parsing4j.etaengine.regex.EtaQuantified.EtaQuantifier;

/*
 * @author Florent Guille
 * */
public class EtaRegexTest {

	@Test
	public void test$1() {
		EtaRegex left = new EtaSequence(List.of(new EtaSolidTerminal("A"),
				new EtaQuantified(EtaQuantifier.PLUS, new EtaSolidTerminal("B")), new EtaSolidTerminal("C")));

		EtaRegex right = new EtaSequence(List.of(new EtaSolidTerminal("A"),
				new EtaQuantified(EtaQuantifier.QUESTION_MARK, new EtaSolidTerminal("B")), new EtaSolidTerminal("C")));

		assertEquals(left, left);
		assertNotEquals(left, right);
	}

}