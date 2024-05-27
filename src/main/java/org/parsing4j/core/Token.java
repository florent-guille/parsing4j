package org.parsing4j.core;

import org.parsing4j.etaengine.etaregex.EtaTerminal;

public class Token {

	private EtaTerminal terminal;
	private String data;
	private int line, col;

	public Token(EtaTerminal terminal, String data, int line, int col) {
		this.terminal = terminal;
		this.data = data;
		this.line = line;
		this.col = col;
	}

	public EtaTerminal getTerminal() {
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
		return "Token(" + terminal.getName() + "," + data + "," + line + "," + col + ")";
	}

}