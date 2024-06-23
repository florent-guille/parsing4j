package org.parsing4j.etaengine.regex;

/*
 * @author Florent Guille
 * */
public class EtaEmptyTerminal extends EtaAbstractTerminal {

	public static final EtaEmptyTerminal INSTANCE = new EtaEmptyTerminal();

	private EtaEmptyTerminal() {

	}

	@Override
	public String getRepr() {
		return "Empty";
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaEmptyTerminal;
	}

}
