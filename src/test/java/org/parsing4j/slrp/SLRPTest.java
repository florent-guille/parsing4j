package org.parsing4j.slrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.Test;
import org.parsing4j.core.CharFlow;
import org.parsing4j.core.Token;
import org.parsing4j.core.Utils;
import org.parsing4j.etaengine.parser.EtaParser;
import org.parsing4j.etaengine.parser.EtaParser.ParserReducer;
import org.parsing4j.etaengine.regex.EtaEOFTerminal;
import org.parsing4j.etaengine.regex.EtaSolidTerminal;
import org.parsing4j.frontend.ParserFrontend;
import org.parsing4j.tokenizer.regex.RegexTokenizer;

/*
 * @author Florent Guille
 * */
public class SLRPTest {

	@SuppressWarnings("unchecked")
	@Test
	public void test$1() throws Exception {
		SLRPFrontendBuilder builder = new SLRPFrontendBuilder();

		builder.getParserBuilder().setConflictSolver((state, sourceRules, current, incoming, lookAhead) -> {
			if (lookAhead instanceof EtaEOFTerminal) {
				return null;
			}

			EtaSolidTerminal terminal = (EtaSolidTerminal) lookAhead;
			if (incoming.rule().getTags().get("BIOP").get(0).elements().get(1).getAsString()
					.equals(terminal.getName())) {
				if (isLeftAssoc(incoming.rule().getName())) {
					return incoming;
				}
				return current;
			}

			assert sourceRules.size() == 1;

			int left = incoming.rule().getTags().get("BIOP").get(0).elements().get(0).getAsInt();
			int right = sourceRules.get(0).getConstrainedRule().getRule().getTags().get("BIOP").get(0).elements().get(0)
					.getAsInt();
			if (left == -1 || right == -1)
				return null;
			if (right > left) {
				return current;
			}
			return incoming;

		});

		ParserFrontend<RegexTokenizer, EtaParser> frontend = builder
				.build(Thread.currentThread().getContextClassLoader().getResourceAsStream("parsing_data/lang1.slrp"));

		frontend.getTokenizer().setFilter(token -> token.getTerminal().getId() != -1);

		ParserReducer reducer = (rule, items) -> {
			if (rule.getTags().containsKey("BIOP") && items.size() == 1) {
				return items.get(0);
			}

			if (rule.getTags().containsKey("BIOP")) {
				TestExpr left = (TestExpr) items.get(0);
				String op = ((Token) items.get(1)).getData();
				TestExpr right = (TestExpr) items.get(2);

				return new BinaryOp(left, op, right);
			}

			if (rule.getName().equals("var")) {
				return new Var(((Token) items.get(0)).getData());
			}

			if (rule.getName().equals("parent")) {
				return items.get(1);
			}

			if (rule.getName().equals("list")) {
				List<TestExpr> result = new ArrayList<>(items.size() >> 1);
				for (int i = 0; i < items.size(); i += 2) {
					result.add((TestExpr) items.get(i));
				}
				return result;
			}

			if (rule.getName().equals("func_call")) {
				return new FuncCall(((Token) items.get(0)).getData(), (List<TestExpr>) items.get(2));
			}

			if (rule.getName().equals("integer")) {
				return new CInteger(Integer.parseInt(((Token) items.get(0)).getData()));
			}

			throw new Exception("Uncaught rule %s".formatted(rule.getName()));
		};

		frontend.getParser().setReducer(reducer);

		CharFlow flow = new CharFlow(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("parsing_data/data_2.txt"));

		TestExpr result = (TestExpr) frontend.parse(flow);

		System.out.println(Utils.toStringTree(result, TestExpr::getRepr, TestExpr::getChildren));
	}

	public boolean isLeftAssoc(String name) {
		if (Objects.equals(name, "+")) {
			return false;
		}

		if (Objects.equals(name, "-")) {
			return true;
		}

		if (Objects.equals(name, "*")) {
			return false;
		}

		if (Objects.equals(name, "/")) {
			return true;
		}

		return false;
	}

	private abstract static class TestExpr {

		protected abstract List<TestExpr> getChildren();

		protected abstract String getRepr();

	}

	private static class BinaryOp extends TestExpr {

		private String op;
		private TestExpr left, right;

		private BinaryOp(TestExpr left, String op, TestExpr right) {
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

	private static class FuncCall extends TestExpr {
		private String name;
		private List<TestExpr> values;

		public FuncCall(String name, List<TestExpr> values) {
			this.name = name;
			this.values = values;
		}

		@Override
		protected List<TestExpr> getChildren() {
			return values;
		}

		@Override
		protected String getRepr() {
			return "FuncCall(" + name + ")";
		}

	}

	private static class CInteger extends TestExpr {

		private int value;

		public CInteger(int value) {
			this.value = value;
		}

		@Override
		protected List<TestExpr> getChildren() {
			return List.of();
		}

		@Override
		protected String getRepr() {
			return "Int(" + value + ")";
		}

	}
}
