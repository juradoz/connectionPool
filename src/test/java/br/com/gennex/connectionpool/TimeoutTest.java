package br.com.gennex.connectionpool;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TimeoutTest {

	private static final int value = (int) (1 + Math.random() * 100);

	private Timeout timeout;

	@Before
	public void setUp() throws Exception {
		timeout = new Timeout(value);
	}

	@Test
	public void testValue() {
		assertEquals(value, timeout.getValue());
	}

}
