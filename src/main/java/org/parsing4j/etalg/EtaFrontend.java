package org.parsing4j.etalg;

import org.parsing4j.core.CharFlow;
import org.parsing4j.etaengine.etaparser.EtaParser;
import org.parsing4j.tokenizer.Tokenizer;

public class EtaFrontend {

	private Tokenizer tokenizer;
	private EtaParser parser;

	public EtaFrontend(Tokenizer tokenizer, EtaParser parser) {
		this.tokenizer = tokenizer;
		this.parser = parser;
	}
	
	public Object parse(CharFlow flow) throws Exception{
		return parser.parse(tokenizer.tokenizeFlow(flow));
	}

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	public EtaParser getParser() {
		return parser;
	}
}
