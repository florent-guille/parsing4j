package org.parsing4j.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.Test;
import org.parsing4j.core.CharFlow.CharFlowException;
import org.parsing4j.core.CharFlow.EndOfStreamReachedException;
import org.parsing4j.core.CharFlow.UnexpectedCharException;
import org.parsing4j.core.CharFlow.UnexpectedEndOfStreamReachedException;

/*
 * @author Florent Guille
 * */
public class CharFlowTest {

	@Test
	public void test$1() throws EndOfStreamReachedException, IOException {
		CharFlow flow = new CharFlow("test");

		assertEquals('t', flow.peek());
		flow.next();

		assertEquals('e', flow.peek());
		flow.next();

		assertEquals('s', flow.peek());
		flow.next();

		assertEquals('t', flow.peek());
		flow.next();

		assertFalse(flow.hasMore());

		assertThrows(EndOfStreamReachedException.class, flow::next);
	}

	@Test
	public void test$2() throws IOException, CharFlowException {
		CharFlow flow = new CharFlow("zyxwvabcde");

		assertEquals('z', flow.peek());
		flow.next();

		flow.eat('y');
		flow.eat('x');

		assertThrows(UnexpectedCharException.class, () -> flow.eat('f'));
		flow.eat('w');

		flow.eat('v');
		assertThrows(UnexpectedCharException.class, () -> flow.eat('c'));
		flow.eat('a');

		flow.next();
		flow.next();
		assertEquals('d', flow.peek());
		flow.eat('d');
		flow.eat('e');

		assertThrows(UnexpectedEndOfStreamReachedException.class, () -> flow.eat('f'));
	}

	@Test
	public void test$3() throws IOException, EndOfStreamReachedException {
		CharFlow flow = new CharFlow("abcde\n\net\ntes\n");

		assertEquals(0, flow.getColumn());
		assertEquals(0, flow.getLine());

		flow.next();
		flow.next();
		flow.next();
		flow.next();
		flow.next();

		assertEquals(5, flow.getColumn());
		assertEquals(0, flow.getLine());

		flow.next();
		assertEquals(0, flow.getColumn());
		assertEquals(1, flow.getLine());

		flow.next();
		assertEquals(0, flow.getColumn());
		assertEquals(2, flow.getLine());

		flow.next();
		flow.next();
		flow.next();
		flow.next();
		flow.next();
		flow.next();
		assertEquals(3, flow.getColumn());
		assertEquals(3, flow.getLine());

		flow.next();
		assertEquals(0, flow.getColumn());
		assertEquals(4, flow.getLine());
	}

}
