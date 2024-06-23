package org.parsing4j.etaengine.parser;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Token;
import org.parsing4j.core.Utils;
import org.parsing4j.etaengine.parser.EtaParser.ParserReducer;
import org.parsing4j.tokenizer.Tokenizer;
import org.parsing4j.tokenizer.regex.MatchingPolicy;
import org.parsing4j.tokenizer.regex.RegexTokenizerBuilder;

/*
 * @author Florent Guille
 * */
public class EtaParserBuilderTest {

	@Test
	public void test$1() throws Exception {
		EtaParserBuilder builder = new EtaParserBuilder();
		builder.addRawRule("main", "S", "E");
		builder.addRawRule("biop", "E", "T ('+' E)?");
		builder.addRawRule("biop", "T", "(T '-')? M");
		builder.addRawRule("biop", "M", "F ('*' M)?");
		builder.addRawRule("biop", "F", "(F '/')? U");
		builder.addRawRule("parent", "U", "'(' E ')'");
		builder.addRawRule("var", "U", "'id'");

		EtaParser parser = builder.build("S");

		ParserReducer reducer = (rule, items) -> {
			if (rule.getName().equals("biop") && items.size() == 1) {
				return items.get(0);
			}

			if (rule.getName().equals("biop")) {
				TestExpr left = (TestExpr) items.get(0);
				String op = ((Token) items.get(1)).getData();
				TestExpr right = (TestExpr) items.get(2);

				return new BinaryOp(op, left, right);
			}

			if (rule.getName().equals("var")) {
				return new Var(((Token) items.get(0)).getData());
			}

			if (rule.getName().equals("parent")) {
				return items.get(1);
			}
			throw new Exception("Unknown rule %s".formatted(rule.getName()));
		};

		parser.setReducer(reducer);

		RegexTokenizerBuilder tokenizerBuilder = new RegexTokenizerBuilder();
		tokenizerBuilder.addPattern("id", "[a-zA-Z][0-9a-zA-Z]*", MatchingPolicy.GREEDY, Set.of());
		tokenizerBuilder.addPattern("+", "'+'", MatchingPolicy.GREEDY, Set.of());
		tokenizerBuilder.addPattern("-", "'-'", MatchingPolicy.GREEDY, Set.of());
		tokenizerBuilder.addPattern("*", "'*'", MatchingPolicy.GREEDY, Set.of());
		tokenizerBuilder.addPattern("/", "'/'", MatchingPolicy.GREEDY, Set.of());
		tokenizerBuilder.addPattern("(", "'('", MatchingPolicy.GREEDY, Set.of());
		tokenizerBuilder.addPattern(")", "')'", MatchingPolicy.GREEDY, Set.of());
		tokenizerBuilder.addPattern("blank", "{Blank}+", MatchingPolicy.GREEDY, Set.of());
		tokenizerBuilder.addPattern("comment-oneline", "//[^,\n]*\n?", MatchingPolicy.GREEDY, Set.of());
		tokenizerBuilder.addPattern("comment-multiline", "'/*'{Any}*'*/'", MatchingPolicy.RELUCTANT, Set.of());

		Tokenizer tokenizer = tokenizerBuilder.build(parser::getTerminal);
		tokenizer.setFilter(token -> token.getTerminal().getId() != -1);

		CharFlow flow = new CharFlow(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("parsing_data/data_1.txt"));

		TestExpr result = (TestExpr) parser.parse(tokenizer.iterator(flow));

		System.out.println(Utils.toStringTree(result, TestExpr::getRepr, TestExpr::getChildren));
	}

	private abstract static class TestExpr {

		protected abstract List<TestExpr> getChildren();

		protected abstract String getRepr();

	}

	private static class BinaryOp extends TestExpr {

		private String op;
		private TestExpr left, right;

		private BinaryOp(String op, TestExpr left, TestExpr right) {
			this.op = op;
			this.left = left;
			this.right = right;
		}

		@Override
		protected List<TestExpr> getChildren() {
			return List.of(left, right);
		}

		@Override
		protected String getRepr() {
			return "BinaryOp(" + op + ")";
		}

	}

	private static class Var extends TestExpr {

		private String name;

		public Var(String name) {
			this.name = name;
		}

		@Override
		protected List<TestExpr> getChildren() {
			return List.of();
		}

		@Override
		protected String getRepr() {
			return "Var(" + name + ")";
		}
	}
}