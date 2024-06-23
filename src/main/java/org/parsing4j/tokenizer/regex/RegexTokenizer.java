package org.parsing4j.tokenizer.regex;

import java.io.IOException;
import java.util.List;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.CharFlow.CharFlowException;
import org.parsing4j.core.Token;
import org.parsing4j.etaengine.regex.EtaEOFTerminal;
import org.parsing4j.tokenizer.MismatchTokenizerException;
import org.parsing4j.tokenizer.Tokenizer;
import org.parsing4j.tokenizer.TokenizerException;

/*
 * @author Florent Guille
 * */
public class RegexTokenizer extends Tokenizer {

	private List<RegexTokenizerNode> nodes;

	public RegexTokenizer(List<RegexTokenizerNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public Token nextToken(CharFlow flow) throws IOException, CharFlowException, TokenizerException {
		RegexTokenizerNode current = nodes.get(0);
		StringBuilder builder = new StringBuilder();
		int line = flow.getLine(), col = flow.getColumn();

		if (!flow.hasMore()) {
			return new Token(EtaEOFTerminal.INSTANCE, null, line, col);
		}
		while (flow.hasMore()) {
			if (current.getPattern() != null && current.getPattern().left == MatchingPolicy.RELUCTANT) {
				break;
			}

			int target = current.getTree().search(flow.peek());

			if (target < 0) {
				break;
			}
			builder.append((char) flow.next());
			current = nodes.get(target);
		}

		if (current.getPattern() == null) {
			throw new MismatchTokenizerException(line, col, builder.toString());
		}

		return new Token(current.getPattern().right, builder.toString(), line, col);
	}

}