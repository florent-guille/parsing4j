package org.parsing4j.etaengine.parser;

import java.util.Objects;
import java.util.Set;

import org.parsing4j.etaengine.regex.EtaAbstractTerminal;

/*
 * @author Florent Guille
 * */
public class EtaMarkedRule {

	private EtaConstrainedRule constrainedRule;
	private int index;

	public EtaMarkedRule(EtaConstrainedRule constrainedRule, int index) {
		this.constrainedRule = constrainedRule;
		this.index = index;
	}

	public EtaNode getCurrentNode() {
		return constrainedRule.getRule().getNodes().get(index);
	}

	public Set<EtaAbstractTerminal> getCurrentFollowSet() {
		return constrainedRule.getFollowSets().get(index);
	}

	public int getIndex() {
		return index;
	}

	public EtaConstrainedRule getConstrainedRule() {
		return constrainedRule;
	}

	@Override
	public int hashCode() {
		return Objects.hash(constrainedRule, index);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof EtaMarkedRule markedRule && Objects.equals(this.constrainedRule, markedRule.constrainedRule)
				&& this.index == markedRule.index;
	}

	@Override
	public String toString() {
		return "EtaMarkedRule(" + constrainedRule.getRule().getName() + ", " + index + ", "
				+ constrainedRule.getLookAheads() + ")";
	}
}