package org.parsing4j.tokenizer.regex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.parsing4j.core.IntIdentifiable;
import org.parsing4j.core.Utils.Pair;
import org.parsing4j.etaengine.regex.EtaSolidTerminal;

/*
 * @author Florent Guille
 * */
public class RegexTokenizerBuildNode extends IntIdentifiable {

	private Map<RegexRange, Set<RegexTokenizerBuildNode>> transitions;
	private Set<RegexTokenizerBuildNode> epsilonTransitions;
	private Pair<MatchingPolicy, EtaSolidTerminal> pattern;

	public RegexTokenizerBuildNode(int id) {
		super(id);
		this.transitions = new HashMap<>();
		this.epsilonTransitions = new HashSet<>();
	}

	public void setPattern(Pair<MatchingPolicy, EtaSolidTerminal> pattern) {
		this.pattern = pattern;
	}

	public Pair<MatchingPolicy, EtaSolidTerminal> getPattern() {
		return pattern;
	}

	public void addTransition(RegexRange range, RegexTokenizerBuildNode target) {
		if (!transitions.containsKey(range)) {
			transitions.put(range, new HashSet<>());
		}
		transitions.get(range).add(target);
	}

	public void addEpsilonTransition(RegexTokenizerBuildNode target) {
		epsilonTransitions.add(target);
	}

	public Map<RegexRange, Set<RegexTokenizerBuildNode>> getTransitions() {
		return transitions;
	}

	public Set<RegexTokenizerBuildNode> getEpsilonTransitions() {
		return epsilonTransitions;
	}

	public String graphvizRepr(String prefix) {
		List<String> lines = new ArrayList<>();

		if (pattern != null) {
			lines.add(prefix + id + " [peripheries=2,label=\"" + pattern.right.getName() + "\"]");
		}

		for (Entry<RegexRange, Set<RegexTokenizerBuildNode>> transition : transitions.entrySet()) {
			for (RegexTokenizerBuildNode target : transition.getValue()) {
				lines.add(prefix + id + " -> " + prefix + target.id + " [label=\"" + transition.getKey() + "\"]");
			}
		}

		for (RegexTokenizerBuildNode target : epsilonTransitions) {
			lines.add(prefix + id + " -> " + prefix + target.id + " [label=\"epsilon\"]");
		}

		return lines.stream().collect(Collectors.joining("\n"));
	}

	public static List<RegexTokenizerNode> determinize(RegexTokenizerBuildNode buildRoot,
			List<RegexTokenizerBuildNode> buildNodes) throws Exception {
		List<Set<RegexTokenizerBuildNode>> closures = new ArrayList<>(buildNodes.size());

		for (RegexTokenizerBuildNode node : buildNodes) {
			closures.add(new HashSet<>(Set.of(node)));
		}

		boolean changed = true;
		while (changed) {
			changed = false;
			for (RegexTokenizerBuildNode node : buildNodes) {
				for (RegexTokenizerBuildNode target : node.getEpsilonTransitions()) {
					changed |= closures.get(node.getId()).addAll(closures.get(target.getId()));
				}
			}
		}

		Map<Set<RegexTokenizerBuildNode>, RegexTokenizerNode> nodeMap = new HashMap<>();
		Deque<Set<RegexTokenizerBuildNode>> stack = new ArrayDeque<>();
		Deque<RegexTokenizerNode> nodes = new ArrayDeque<>();

		RegexTokenizerNode root = new RegexTokenizerNode(0);
		nodeMap.put(closures.get(buildRoot.getId()), root);
		stack.push(closures.get(buildRoot.getId()));
		nodes.push(root);

		while (!stack.isEmpty()) {
			Set<RegexTokenizerBuildNode> currentSet = stack.pop();
			RegexTokenizerNode currentNode = nodeMap.get(currentSet);

			Pair<MatchingPolicy, EtaSolidTerminal> pattern = null;
			for (RegexTokenizerBuildNode node : currentSet) {
				if (node.getPattern() == null)
					continue;

				if (pattern == null) {
					pattern = node.getPattern();
					continue;
				}

				if (!Objects.equals(pattern, node.pattern)) {
					throw new Exception(
							"Conflict between patterns %s and %s".formatted(pattern.right, node.pattern.right));
				}

			}

			currentNode.setPattern(pattern);

			List<Pair<RegexRange, Set<RegexTokenizerBuildNode>>> markedRanges = new ArrayList<>();

			for (RegexTokenizerBuildNode node : currentSet) {
				for (Entry<RegexRange, Set<RegexTokenizerBuildNode>> transition : node.getTransitions().entrySet()) {
					markedRanges.add(new Pair<>(transition.getKey(), transition.getValue()));
				}
			}

			List<Pair<RegexRange, Set<RegexTokenizerBuildNode>>> segmented = RegexRange.segmentMarked(markedRanges,
					buildNodes);

			for (Pair<RegexRange, Set<RegexTokenizerBuildNode>> transition : segmented) {
				Set<RegexTokenizerBuildNode> completed = new HashSet<>();
				for (RegexTokenizerBuildNode node : transition.right) {
					completed.addAll(closures.get(node.getId()));
				}

				RegexTokenizerNode target = nodeMap.get(completed);
				if (target == null) {
					target = new RegexTokenizerNode(nodes.size());
					nodes.add(target);
					nodeMap.put(completed, target);
					stack.push(completed);
				}
				currentNode.getTree().insertRange(transition.left, target.getId());
			}
		}

		return new ArrayList<>(nodes);
	}

}