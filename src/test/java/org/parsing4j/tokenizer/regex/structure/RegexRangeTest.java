package org.parsing4j.tokenizer.regex.structure;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.Test;
import org.parsing4j.core.IntIdentifiable;
import org.parsing4j.core.Utils.Pair;
import org.parsing4j.tokenizer.regex.structure.RegexRange;

public class RegexRangeTest {

	@Test
	public void segmentTest$1() {
		List<RegexRange> ranges = List.of(new RegexRange(2, 15), new RegexRange(7, 20), new RegexRange(4, 8),
				new RegexRange(25, 30));
		List<RegexRange> segmented = RegexRange.segment(ranges);

		List<RegexRange> expected = List.of(new RegexRange(2, 20), new RegexRange(25, 30));
		assertEquals(expected, segmented);
	}

	@Test
	public void invertTest$1() {
		List<RegexRange> ranges = List.of(new RegexRange(2, 15), new RegexRange(7, 20), new RegexRange(4, 8),
				new RegexRange(25, 30));
		List<RegexRange> segmented = RegexRange.segment(ranges);
		List<RegexRange> inverted = RegexRange.invert(segmented, 0, 255);
		List<RegexRange> expected = List.of(new RegexRange(0, 1), new RegexRange(21, 24), new RegexRange(31, 255));
		assertEquals(expected, inverted);
	}

	@Test
	public void invertTest$2() {
		List<RegexRange> ranges = List.of(new RegexRange(0, 7), new RegexRange(8, 20), new RegexRange(21, 25));
		List<RegexRange> segmented = RegexRange.segment(ranges);
		List<RegexRange> inverted = RegexRange.invert(segmented, 0, 255);
		List<RegexRange> expected = List.of(new RegexRange(26, 255));
		assertEquals(expected, inverted);
	}

	@Test
	public void segmentMarkedTest$1() {
		List<IntIdentifiable> markers = IntStream.range(0, 6).mapToObj(IntIdentifiable::new).toList();
		List<Pair<RegexRange, Set<IntIdentifiable>>> ranges = List.of(
				new Pair<>(new RegexRange(5, 20), Set.of(markers.get(0), markers.get(1))), //
				new Pair<>(new RegexRange(15, 21),
						Set.of(markers.get(4), markers.get(0), markers.get(2), markers.get(1))), //
				new Pair<>(new RegexRange(2, 17), Set.of(markers.get(5))), //
				new Pair<>(new RegexRange(3, 45), Set.of(markers.get(3))));

		List<Pair<RegexRange, Set<IntIdentifiable>>> segmented = RegexRange.segmentMarked(ranges, markers);

		List<Pair<RegexRange, Set<IntIdentifiable>>> expected = List.of(//
				new Pair<>(new RegexRange(2, 2), Set.of(markers.get(5))), //
				new Pair<>(new RegexRange(3, 4), Set.of(markers.get(3), markers.get(5))), //
				new Pair<>(new RegexRange(5, 14),
						Set.of(markers.get(0), markers.get(1), markers.get(3), markers.get(5))), //
				new Pair<>(new RegexRange(15, 17),
						Set.of(markers.get(0), markers.get(1), markers.get(2), markers.get(3), markers.get(4),
								markers.get(5))), //
				new Pair<>(new RegexRange(18, 21),
						Set.of(markers.get(0), markers.get(1), markers.get(2), markers.get(3), markers.get(4))),
				new Pair<>(new RegexRange(22, 45), Set.of(markers.get(3))));

		assertEquals(expected, segmented);
	}
}
