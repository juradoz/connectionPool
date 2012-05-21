package net.danieljurado.connectionpool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.security.InvalidParameterException;

import org.junit.Test;

public class IntegerGreaterThanZeroTest {

	private static final int positive = Integer.MAX_VALUE;
	private static final int negative = Integer.MIN_VALUE;
	private static final int zero = 0;

	private IntegerGreaterThanZero integerGreaterThanZero;

	@Test
	public void testPositive() {
		integerGreaterThanZero = new IntegerGreaterThanZero(positive);
		assertEquals(positive, integerGreaterThanZero.getValue());
	}

	@Test(expected = InvalidParameterException.class)
	public void testNegative() {
		integerGreaterThanZero = new IntegerGreaterThanZero(negative);
		fail();
	}

	@Test(expected = InvalidParameterException.class)
	public void testZero() {
		integerGreaterThanZero = new IntegerGreaterThanZero(zero);
		fail();
	}
}
