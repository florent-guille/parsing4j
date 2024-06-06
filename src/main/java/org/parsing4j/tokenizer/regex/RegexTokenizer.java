package org.parsing4j.tokenizer.regex;

import java.util.List;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Token;
import org.parsing4j.tokenizer.Tokenizer;

public class RegexTokenizer extends Tokenizer {

	private List<RegexTokenizerNode> nodes;

	public RegexTokenizer(List<RegexTokenizerNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public Token nextToken(CharFlow flow) throws Exception {
		RegexTokenizerNode current = nodes.get(0);
		StringBuilder builder = new StringBuilder();
		int line = flow.getLine(), col = flow.getCol();

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
			throw new Exception(
					"Unable to identify at line %s, col %s: \"%s\"".formatted(line, col, builder.toString()));
		}

		return new Token(current.getPattern().right, builder.toString(), line, col);
	}

}
