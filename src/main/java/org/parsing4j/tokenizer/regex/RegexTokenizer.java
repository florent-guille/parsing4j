package org.parsing4j.tokenizer.regex;

import java.util.List;

import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Token;
import org.parsing4j.core.Utils;
import org.parsing4j.etaengine.etaregex.EtaTerminal;
import org.parsing4j.tokenizer.Tokenizer;
import org.parsing4j.tokenizer.regex.structure.RegexRangeTreeNode;

public class RegexTokenizer extends Tokenizer {

	private List<RegexTokenizerNode> nodes;

	public RegexTokenizer(List<RegexTokenizerNode> nodes, EtaTerminal eof) {
		super(eof);
		this.nodes = nodes;
		this.eof = eof;
	}

	@Override
	public Token nextToken(CharFlow flow) throws Exception {
		RegexTokenizerNode current = nodes.get(0);
		StringBuilder builder = new StringBuilder();
		int line = flow.getLine(), col = flow.getCol();

		if (!flow.hasMore()) {
			return new Token(eof, null, line, col);
		}
		while (flow.hasMore()) {
			if (current.getPattern() != null && current.getPattern().left == MatchingPolicy.RELUCTANT) {
				break;
			}

			int target = current.getTree().search(flow.peek());
			System.out.println(Utils.treeRepr(current.getTree().getRoot(), RegexRangeTreeNode::toString,
					RegexRangeTreeNode::getChildren));

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
