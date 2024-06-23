package org.parsing4j.core;

import org.parsing4j.etaengine.regex.EtaAbstractTerminal;

/*
 * @author Florent Guille
 * */
public class Token {

	private EtaAbstractTerminal terminal;
	private String data;
	private int line, col;

	public Token(EtaAbstractTerminal terminal, String data, int line, int col) {
		this.terminal = terminal;
		this.data = data;
		this.line = line;
		this.col = col;
	}

	public EtaAbstractTerminal getTerminal() {
		return terminal;
	}

	public String getData() {
		return data;
	}

	public int getLine() {
		return line;
	}

	public int getCol() {
		return col;
	}

	@Override
	public String toString() {
		return "Token(" + terminal + "," + data + "," + line + "," + col + ")";
	}

}