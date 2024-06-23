package org.parsing4j.tokenizer;

import java.io.IOException;
import java.util.Iterator;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.CharFlow.CharFlowException;
import org.parsing4j.core.Token;
import org.parsing4j.core.Utils.Filter;

/*
 * @author Florent Guille
 * */
public abstract class Tokenizer {

	private Filter<Token> filter;

	public Tokenizer() {
		this.filter = token -> true;
	}

	public void setFilter(Filter<Token> filter) {
		this.filter = filter;
	}

	public abstract Token nextToken(CharFlow flow) throws IOException, CharFlowException, TokenizerException;

	public Token nextTokenFiltered(CharFlow flow) throws IOException, CharFlowException, TokenizerException {
		Token result = nextToken(flow);

		while (!filter.isValid(result)) {
			result = nextToken(flow);
		}

		return result;
	}

	public Iterator<Token> iterator(CharFlow flow) {
		return new TokenizerIterator(this, flow);
	}
}