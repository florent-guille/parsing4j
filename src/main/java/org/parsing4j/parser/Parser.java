package org.parsing4j.parser;

import java.util.Iterator;

import org.parsing4j.core.Token;

/*
 * @author Florent Guille
 * */
public abstract class Parser {

	public Parser() {

	}

	public Object parse(Iterable<Token> iterable) throws Exception {
		return parse(iterable.iterator());
	}

	public abstract Object parse(Iterator<Token> iterator) throws Exception;

}
