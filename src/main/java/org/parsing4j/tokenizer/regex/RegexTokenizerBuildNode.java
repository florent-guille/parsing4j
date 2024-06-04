package org.parsing4j.tokenizer.regex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.parsing4j.core.IntIdentifiable;

public class RegexTokenizerBuildNode extends IntIdentifiable{
	
	private Map<RegexRange, Set<RegexTokenizerBuildNode>> transitions;
	private Set<RegexTokenizerBuildNode> epsilonTransitions;

	public RegexTokenizerBuildNode(int id) {
		super(id);
		this.transitions = new HashMap<>();
		this.epsilonTransitions = new HashSet<>();
	}
	
	public void addTransition(RegexRange range, RegexTokenizerBuildNode target) {
		if(!transitions.containsKey(range)) {
			transitions.put(range,new HashSet<>());
		}
		transitions.get(range).add(target);
	}
	
	public void addEpsilonTransition(RegexTokenizerBuildNode target) {
		epsilonTransitions.add(target);
	}

}