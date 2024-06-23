package org.parsing4j.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Wrapper around {@link java.io.Reader} providing a basic tool set for parsing
 * 
 * @author Florent Guille
 */
public class CharFlow {

	/** Target wrapped reader of this CharFlow **/
	private Reader reader;

	/** Last seen character **/
	private int current;

	/** Current line the reader is at **/
	private int line;

	/** Current column the reader is at **/
	private int column;

	public CharFlow(Reader reader) {
		this.reader = reader;
		this.current = -2;
		this.line = 0;
		this.column = 0;
	}

	public CharFlow(InputStream stream, Charset c) {
		this(new InputStreamReader(stream, c));
	}

	public CharFlow(InputStream stream) {
		this(stream, StandardCharsets.UTF_8);
	}

	public CharFlow(String input) {
		this(new StringReader(input));
	}

	public int peek() throws IOException {
		if (current == -2) {
			current = reader.read();
		}
		return current;
	}

	public void eat(int target) throws IOException, CharFlowException {
		if (!hasMore()) {
			throw new UnexpectedEndOfStreamReachedException(line, column, target);
		}

		if (peek() != target) {
			throw new UnexpectedCharException(line, column, target, peek());
		}

		step();
	}

	public int next() throws IOException, EndOfStreamReachedException {
		if (!hasMore()) {
			throw new EndOfStreamReachedException(line, column);
		}
		int result = peek();
		step();
		return result;
	}

	public void skipBlanks() throws IOException {
		while (hasMore() && Character.isWhitespace(peek())) {
			step();
		}
	}

	private void step() throws IOException {
		if (peek() == '\n') {
			line++;
			column = 0;
		} else {
			column++;
		}
		current = -2;
	}

	public boolean hasMore() throws IOException {
		return peek() != -1;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	@SuppressWarnings("serial")
	/**
	 * Common class for CharFlow Exception. This holds data about the position of
	 * the reader where the exception occured
	 * 
	 * @author Florent Guille
	 **/
	public static class CharFlowException extends Exception {
		protected int line, column;

		private CharFlowException(int line, int column) {
			super();
			this.line = line;
			this.column = column;
		}

		public int getLine() {
			return line;
		}

		public int getColumn() {
			return column;
		}
	}

	@SuppressWarnings("serial")
	public static class ExpectingCharFlowException extends CharFlowException {
		protected int expected;

		private ExpectingCharFlowException(int line, int column, int expected) {
			super(line, column);
			this.expected = expected;
		}

		public int getExpected() {
			return expected;
		}
	}

	@SuppressWarnings("serial")
	public static class UnexpectedEndOfStreamReachedException extends ExpectingCharFlowException {

		public UnexpectedEndOfStreamReachedException(int line, int column, int expected) {
			super(line, column, expected);
		}

		@Override
		public String getMessage() {
			return "At line %s, column %s, expected '%s' (%s) but reached end of stream".formatted(line, column,
					(char) expected, expected);
		}

	}

	@SuppressWarnings("serial")
	public static class UnexpectedCharException extends CharFlowException {

		private int expected, obtained;

		public UnexpectedCharException(int line, int column, int expected, int obtained) {
			super(line, column);
			this.expected = expected;
			this.obtained = obtained;
		}

		@Override
		public String getMessage() {
			return "At line %s, column %s, expected '%s' (%s) but got '%s' (%s)".formatted(line, column,
					(char) expected, expected, (char) obtained, obtained);
		}

		public int getExpected() {
			return expected;
		}

		public int getObtained() {
			return obtained;
		}
	}

	@SuppressWarnings("serial")
	public static class UnwantedCharException extends CharFlowException {

		private int obtained;

		public UnwantedCharException(int line, int column, int obtained) {
			super(line, column);
			this.obtained = obtained;
		}

		@Override
		public String getMessage() {
			return "At line %s, column %s, got unwanted character '%s' (%s)".formatted(line, column, (char) obtained,
					obtained);
		}
	}

	@SuppressWarnings("serial")
	public static class EndOfStreamReachedException extends CharFlowException {

		public EndOfStreamReachedException(int line, int column) {
			super(line, column);
		}

		@Override
		public String getMessage() {
			return "At line %s, column %s, end of stream reached while trying to advance".formatted(line, column);
		}
	}
}
