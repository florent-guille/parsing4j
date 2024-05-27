package org.parsing4j.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.parsing4j.core.CharFlow.EndReachedException;
import org.parsing4j.core.CharFlow.UnexpectedCharException;
import org.parsing4j.core.Utils.Pair;

public class CharFlowTest {

	@Test
	public void charflowTest$Valid1() throws Exception {
		List<Pair<String, char[]>> testCases = new ArrayList<>();

		testCases.add(new Pair<>("abcdefgh", new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' }));

		for (Pair<String, char[]> testCase : testCases) {
			CharFlow flow = new CharFlow(testCase.left);

			for (char c : testCase.right) {
				assertEquals(c, flow.next());
			}

			assertFalse(flow.hasMore());
		}
	}

	@Test
	public void charflowTest$FailUnexpected1() throws Exception {
		List<String> inputs = new ArrayList<>();
		inputs.add("abcdef");
		inputs.add("efgeedf");
		inputs.add("mquhguipe");

		for (String input : inputs) {
			for (int i = 0; i < input.length(); i++) {
				CharFlow flow = new CharFlow(input);
				for (int j = 0; j < i; j++) {
					flow.eat(input.charAt(j));
				}

				assertThrows(UnexpectedCharException.class, () -> {
					flow.eat('z');
				});
			}
		}
	}

	@Test
	public void charflowTest$Normal1() throws Exception {
		List<String> inputs = new ArrayList<>();
		inputs.add("qgspsndjgr");
		inputs.add("djdsjflffsnsjf");
		inputs.add("dfsfqir");

		for (String input : inputs) {
			CharFlow flow = new CharFlow(input);

			for (int i = 0; i < input.length(); i++) {
				assertEquals(input.charAt(i), flow.next());
			}

			assertThrows(EndReachedException.class, () -> {
				flow.next();
			});
		}
	}

}