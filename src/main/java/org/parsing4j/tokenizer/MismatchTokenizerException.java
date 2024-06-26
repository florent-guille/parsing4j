package org.parsing4j.tokenizer;

@SuppressWarnings("serial")
public class MismatchTokenizerException extends TokenizerException {

	private int line, column;
	private String data;

	public MismatchTokenizerException(int line, int column, String data) {
		this.line = line;
		this.column = column;
		this.data = data;
	}

	@Override
	public String getMessage() {
		return "At line %s, column %s, unable to identify \"%s\"".formatted(line, column, data);
	}

}
