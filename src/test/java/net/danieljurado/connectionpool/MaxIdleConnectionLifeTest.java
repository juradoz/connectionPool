package net.danieljurado.connectionpool;

import static org.junit.Assert.assertEquals;

import net.danieljurado.connectionpool.old.MaxIdleConnectionLife;

import org.junit.Before;
import org.junit.Test;

public class MaxIdleConnectionLifeTest {

	private static final int value = (int) (1 + Math.random() * 100);

	private MaxIdleConnectionLife maxIdleConnectionLife;

	@Before
	public void setUp() throws Exception {
		maxIdleConnectionLife = new MaxIdleConnectionLife(value);
	}

	@Test
	public void testValue() {
		assertEquals(value, maxIdleConnectionLife.getValue());
	}

}
