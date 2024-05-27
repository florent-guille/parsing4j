package org.parsing4j.tokenizer;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Token;
import org.parsing4j.core.Utils.Filter;

public abstract class Tokenizer {

	private Filter<Token> filter;

	protected Tokenizer() {
		this.filter = token -> token.getTerminal().getId() != -1;
	}

	public void setFilter(Filter<Token> filter) {
		this.filter = filter;
	}

	public abstract Token nextToken(CharFlow flow) throws Exception;

	public Token nextTokenFiltered(CharFlow flow) throws Exception {
		Token result = nextToken(flow);
		while (!filter.isValid(result)) {
			result = nextToken(flow);
		}
		return result;
	}

}
