package org.parsing4j.tokenizer;

import java.util.List;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Token;

public class ListTokenizer extends Tokenizer {

	private List<Token> tokens;
	private int index;

	public ListTokenizer(List<Token> tokens) {
		this.tokens = tokens;
		this.index = 0;
	}

	@Override
	public Token nextToken(CharFlow flow) throws Exception {
		return tokens.get(index++);
	}

}
