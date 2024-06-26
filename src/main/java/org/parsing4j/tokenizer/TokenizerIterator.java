package org.parsing4j.tokenizer;

import java.util.Iterator;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Token;

public class TokenizerIterator implements Iterator<Token> {

	private Tokenizer tokenizer;
	private CharFlow flow;
	private int eofIndex;
	private boolean hasReachedEOF;

	public TokenizerIterator(Tokenizer tokenizer, CharFlow flow, int eofIndex) {
		this.tokenizer = tokenizer;
		this.flow = flow;
		this.eofIndex = eofIndex;
	}

	@Override
	public boolean hasNext() {
		return !hasReachedEOF;
	}

	@Override
	public Token next() {
		try {
			Token result = tokenizer.nextTokenFiltered(flow);
			if (result.getTerminal().getId() == eofIndex) {
				hasReachedEOF = true;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
