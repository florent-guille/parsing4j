package org.parsing4j.tokenizer;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Token;
import org.parsing4j.core.Utils.Filter;
import org.parsing4j.etaengine.etaregex.EtaTerminal;

public abstract class Tokenizer {

	private Filter<Token> filter;
	protected EtaTerminal eof;

	protected Tokenizer(EtaTerminal eof) {
		this.filter = token -> token.getTerminal().getId() != -1;
		this.eof = eof;
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

	public TokenizerIterator tokenizeFlow(CharFlow flow) {
		return new TokenizerIterator(this, flow, eof.getId());
	}

}
