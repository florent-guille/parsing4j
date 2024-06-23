package org.parsing4j.etaengine.regex;

/*
 * @author Florent Guille
 * */
public class EtaEOFTerminal extends EtaAbstractTerminal {
	
	public static final EtaEOFTerminal INSTANCE = new EtaEOFTerminal();

	private EtaEOFTerminal() {
		this.id = 0;
	}

	@Override
	public String getRepr() {
		return "EOF";
	}

	@Override
	public boolean simpleEquals(EtaRegex regex) {
		return regex instanceof EtaEOFTerminal;
	}

}
