package org.parsing4j.etaengine.regex;

import java.util.List;

import org.parsing4j.etaengine.regex.EtaQuantified.EtaQuantifier;

/*
 * @author Florent Guille
 * */
public class EtaRegexBuilder {

	public static EtaSequence seq(EtaRegex... items) {
		return new EtaSequence(List.of(items));
	}

	public static EtaChoice choice(EtaRegex... items) {
		return new EtaChoice(List.of(items));
	}

	public static EtaQuantified star(EtaRegex target) {
		return new EtaQuantified(EtaQuantifier.STAR, target);
	}

	public static EtaQuantified plus(EtaRegex target) {
		return new EtaQuantified(EtaQuantifier.PLUS, target);
	}

	public static EtaQuantified qmark(EtaRegex target) {
		return new EtaQuantified(EtaQuantifier.QUESTION_MARK, target);
	}

	public static EtaSolidTerminal term(String name) {
		return new EtaSolidTerminal(name);
	}

	public static EtaVariable var(String name) {
		return new EtaVariable(name);
	}

	public static EtaBlank blank() {
		return new EtaBlank();
	}
}
