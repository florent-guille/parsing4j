package org.parsing4j.tokenizer.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.parsing4j.core.IntIdentifiable;
import org.parsing4j.core.Utils.Pair;

/*
 * @author Florent Guille
 * */
public class RegexRange implements Comparable<RegexRange> {

	private int start, end;

	public RegexRange(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public RegexRange(int code) {
		this(code, code);
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end);
	}

	@Override
	public String toString() {
		return start + "-" + end;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof RegexRange range && this.start == range.start && this.end == range.end;
	}

	@Override
	public int compareTo(RegexRange o) {
		if (this.start != o.start) {
			return this.start - o.start;
		}
		return this.end - o.end;
	}

	public int compareTo(int i) {
		if (i > end) {
			return -1;
		}
		if (i < start) {
			return 1;
		}
		return 0;
	}

	public static List<RegexRange> segment(List<RegexRange> ranges) {
		if (ranges.isEmpty()) {
			return List.of();
		}

		List<RegexRange> data = new ArrayList<>(ranges);
		Collections.sort(data);

		List<RegexRange> result = new ArrayList<>();

		int previous_start = data.get(0).start;
		int previous_end = data.get(0).end;

		for (int i = 1; i < data.size(); i++) {
			RegexRange range = data.get(i);
			if (range.start > previous_end + 1) {
				result.add(new RegexRange(previous_start, previous_end));
				previous_start = range.start;
				previous_end = range.end;
			} else {
				previous_end = range.end;
			}
		}
		result.add(new RegexRange(previous_start, previous_end));
		return result;
	}

	public static List<RegexRange> invert(List<RegexRange> segmented, int min, int max) {
		if (segmented.isEmpty()) {
			return List.of(new RegexRange(min, max));
		}
		int previous = min - 1;

		List<RegexRange> result = new ArrayList<>();
		for (RegexRange range : segmented) {
			if (range.start > previous + 1) {
				result.add(new RegexRange(previous + 1, range.start - 1));
			}
			previous = range.end;
		}
		if (previous < max) {
			result.add(new RegexRange(previous + 1, max));
		}
		return result;
	}

	public static final Comparator<Pair<RegexRange, ?>> MARKED_RANGE_COMPARATOR = (r1, r2) -> r1.left
			.compareTo(r2.left);

	public static <T extends IntIdentifiable> List<Pair<RegexRange, Set<T>>> segmentMarked(
			List<Pair<RegexRange, Set<T>>> markedRanges, List<T> markers) {

		if (markedRanges.isEmpty()) {
			return List.of();
		}

		List<MarkedPoint<T>> points = new ArrayList<>(2 * markedRanges.size());
		for (Pair<RegexRange, Set<T>> markedRange : markedRanges) {
			points.add(new MarkedPoint<>(markedRange.left.start, markedRange.right, true));
			points.add(new MarkedPoint<>(markedRange.left.end + 1, markedRange.right, false));
		}
		Collections.sort(points);
		List<Pair<RegexRange, Set<T>>> result = new ArrayList<>();

		int[] counts = new int[markers.size()];
		Set<T> acc = new HashSet<>(points.get(0).data);
		for (T item : acc) {
			counts[item.getId()]++;
		}
		int pos = points.get(0).pos;
		boolean isStart = points.get(0).isStart;

		for (int i = 1; i < points.size(); i++) {
			MarkedPoint<T> point = points.get(i);
			Set<T> candidate = null;
			if (!acc.isEmpty() && (i == points.size() - 1 || point.pos != pos || point.isStart != isStart)) {
				candidate = new HashSet<>(acc);
			}

			int size = acc.size();

			if (point.isStart) {
				acc.addAll(point.data);
				for (T item : point.data) {
					counts[item.getId()]++;
				}
			} else {
				for (T item : point.data) {
					counts[item.getId()]--;
					if (counts[item.getId()] == 0) {
						acc.remove(item);
					}
				}
			}

			if ((size != acc.size() || i == points.size() - 1) && candidate != null) {
				result.add(new Pair<>(new RegexRange(pos, point.pos - 1), candidate));

			}

			if (!(candidate != null && size == acc.size())) {
				pos = point.pos;
				isStart = point.isStart;
			}

		}

		return result;
	}

	public static class MarkedPoint<T> implements Comparable<MarkedPoint<T>> {

		private int pos;
		private Set<T> data;
		private boolean isStart;

		public MarkedPoint(int pos, Set<T> data, boolean isStart) {
			this.pos = pos;
			this.data = data;
			this.isStart = isStart;
		}

		@Override
		public int compareTo(MarkedPoint<T> o) {
			if (this.pos != o.pos) {
				return this.pos - o.pos;
			}

			return (this.isStart ? 1 : 0) - (o.isStart ? 1 : 0);
		}

		@Override
		public String toString() {
			return "(" + pos + "," + data + "," + isStart + ")";
		}

	}

}