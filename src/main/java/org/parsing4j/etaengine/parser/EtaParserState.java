package org.parsing4j.etaengine.parser;

import java.util.Set;

import org.parsing4j.etaengine.parser.EtaParser.EtaParserAction;

/*
 * @author Florent Guille
 * */
public class EtaParserState {

	private int id;
	private int[] gotos;
	private EtaParserAction[] actions;
	private Set<EtaRule> activeRules;

	public EtaParserState(int id, EtaParserAction[] actions, int[] gotos, Set<EtaRule> activeRules) {
		this.id = id;
		this.actions = actions;
		this.gotos = gotos;
		this.activeRules = activeRules;
	}

	public int getId() {
		return id;
	}

	public EtaParserAction[] getActions() {
		return actions;
	}

	public int[] getGotos() {
		return gotos;
	}

	public Set<EtaRule> getActiveRules() {
		return activeRules;
	}

	@Override
	public String toString() {
		return "EtaParserState(" + id + ")";
	}
}