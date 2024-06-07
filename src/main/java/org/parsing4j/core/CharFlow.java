package org.parsing4j.core;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class CharFlow {

	private Reader reader;
	private int current = -2;

	private int line, col;

	public CharFlow(Reader reader) {
		this.reader = reader;
	}

	public CharFlow(String data) {
		this.reader = new StringReader(data);
	}

	public int peek() throws IOException {
		if (current == -2) {
			current = reader.read();
		}
		return current;
	}

	public void eat(int target) throws IOException, CharFlowException {
		
		if (!hasMore()) {
			throw new EndReachedException(line, col);
		}
		if (target != peek()) {
			throw new UnexpectedCharException(target, peek(), line, col);
		}
		if (target == '\n') {
			line++;
			col = 0;
		} else {
			col++;
		}

		current = reader.read();
	}

	public int next() throws IOException, CharFlowException {
		int c = peek();
		eat(c);
		return c;
	}

	public boolean hasMore() throws IOException {
		return peek() != -1;
	}

	public void skipBlanks() throws IOException, CharFlowException {
		while ((peek() >= 0x9 && peek() <= 0xd) || peek() == 0x20 || peek() == 0x85 || peek() == 0xA0) {
			next();
		}
	}

	public int getLine() {
		return line;
	}

	public int getCol() {
		return col;
	}

	@SuppressWarnings("serial")
	public static class CharFlowException extends Exception {

	}

	@SuppressWarnings("serial")
	public static class UnexpectedCharException extends CharFlowException {

		private int target, found, line, col;

		public UnexpectedCharException(int target, int found, int line, int col) {
			super();
			this.target = target;
			this.found = found;
			this.line = line;
			this.col = col;
		}

		@Override
		public String getMessage() {
			return "Expected '%s'(%s), got '%s'(%s) at line %s, col %s".formatted((char) target, target, (char) found,
					found, line, col);
		}

	}

	@SuppressWarnings("serial")
	public static class EndReachedException extends CharFlowException {

		private int line, col;

		public EndReachedException(int line, int col) {
			super();
		}

		@Override
		public String getMessage() {
			return "End reached at line %s, col %s".formatted(line, col);
		}
	}
}