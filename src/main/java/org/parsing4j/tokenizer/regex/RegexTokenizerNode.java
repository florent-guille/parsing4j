package org.parsing4j.tokenizer.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.parsing4j.core.IntIdentifiable;
import org.parsing4j.core.Utils.Pair;
import org.parsing4j.etaengine.regex.EtaSolidTerminal;

/*
 * @author Florent Guille
 * */
public class RegexTokenizerNode extends IntIdentifiable {

	private RegexRangeTree tree;
	private Pair<MatchingPolicy, EtaSolidTerminal> pattern;

	public RegexTokenizerNode(int id) {
		super(id);
		this.tree = new RegexRangeTree();
	}

	public void setPattern(Pair<MatchingPolicy, EtaSolidTerminal> pattern) {
		this.pattern = pattern;
	}

	public Pair<MatchingPolicy, EtaSolidTerminal> getPattern() {
		return pattern;
	}

	public RegexRangeTree getTree() {
		return tree;
	}

	public String graphvizRepr(String prefix) {
		List<String> lines = new ArrayList<>();
		if (pattern != null) {
			lines.add(prefix + id + " [peripheries=2,label=\"" + pattern.right.getName() + "\"]");
		}

		for (RegexRangeTreeNode node : tree) {
			lines.add(prefix + id + " -> " + prefix + node.target + "[label=\"" + node.range + "\"]");
		}

		return lines.stream().collect(Collectors.joining("\n"));
	}
}