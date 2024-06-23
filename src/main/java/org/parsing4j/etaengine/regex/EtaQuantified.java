package org.parsing4j.etaengine.regex;

import java.util.List;

/*
 * @author Florent Guille
 * */
public class EtaQuantified extends EtaRegex {

	private EtaQuantifier quantifier;
	private EtaRegex target;

	public EtaQuantified(EtaQuantifier quantifier, EtaRegex target) {
		this.quantifier = quantifier;
		this.target = target;
	}

	public EtaQuantifier getQuantifier() {
		return quantifier;
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaQuantified q && this.quantifier == q.quantifier;
	}

	@Override
	public List<EtaRegex> getChildren() {
		return List.of(target);
	}

	@Override
	public String getRepr() {
		return "EtaQuantified(" + quantifier.name() + ")";
	}

	public static enum EtaQuantifier {
		STAR, PLUS, QUESTION_MARK;

		private EtaQuantifier() {

		}

		public static EtaQuantifier getQuantifier(int c) {
			if (c == '*')
				return STAR;
			if (c == '+')
				return PLUS;
			if (c == '?')
				return QUESTION_MARK;
			return null;
		}
	}

}