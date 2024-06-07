package org.parsing4j.etalg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;
import org.parsing4j.core.CharFlow;

public class ETALG_ParserTest {

	@Test
	public void etalgParserTest$1() throws Exception{
		Reader r = new BufferedReader(new InputStreamReader(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("lang1.etalg")));
		EtaFrontend frontend = ETALG_Parser.parseETALG(new CharFlow(r));
		frontend.parse(new CharFlow("a"));
	}

}
