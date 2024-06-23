package org.parsing4j.tokenizer;

import java.util.Iterator;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Token;
import org.parsing4j.etaengine.regex.EtaEOFTerminal;

/*
 * @author Florent Guille
 * */
public class TokenizerIterator implements Iterator<Token> {

	private Tokenizer tokenizer;
	private CharFlow flow;
	private boolean hasReachedEOF;

	public TokenizerIterator(Tokenizer tokenizer, CharFlow flow) {
		this.tokenizer = tokenizer;
		this.flow = flow;
	}

	@Override
	public boolean hasNext() {
		return !hasReachedEOF;
	}

	@Override
	public Token next() {
		try {
			Token result = tokenizer.nextTokenFiltered(flow);
			if (result.getTerminal() instanceof EtaEOFTerminal) {
				hasReachedEOF = true;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
