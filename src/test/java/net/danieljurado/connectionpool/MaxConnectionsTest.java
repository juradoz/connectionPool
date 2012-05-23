package net.danieljurado.connectionpool;

import static org.junit.Assert.assertEquals;

import net.danieljurado.connectionpool.old.MaxConnections;

import org.junit.Before;
import org.junit.Test;

public class MaxConnectionsTest {

	private static final int value = (int) (1 + Math.random() * 100);

	private MaxConnections maxConnections;

	@Before
	public void setUp() throws Exception {
		maxConnections = new MaxConnections(value);
	}

	@Test
	public void testValue() {
		assertEquals(value, maxConnections.getValue());
	}

}
