package org.parsing4j.frontend;

import org.parsing4j.core.CharFlow;
import org.parsing4j.parser.Parser;
import org.parsing4j.tokenizer.Tokenizer;

/*
 * @author Florent Guille
 * */
public class ParserFrontend<A extends Tokenizer, B extends Parser> {

	private A tokenizer;
	private B parser;

	public ParserFrontend(A tokenizer, B parser) {
		this.tokenizer = tokenizer;
		this.parser = parser;
	}

	public Object parse(CharFlow flow) throws Exception {
		return parser.parse(tokenizer.iterator(flow));
	}

	public A getTokenizer() {
		return tokenizer;
	}

	public B getParser() {
		return parser;
	}
}