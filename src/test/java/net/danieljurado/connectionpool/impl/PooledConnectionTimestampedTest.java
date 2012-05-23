package net.danieljurado.connectionpool.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.sql.PooledConnection;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class PooledConnectionTimestampedTest {

	@Mock
	private PooledConnection pooledConnection;
	private PooledConnectionTimestamped pooledConnectionTimestamped;

	@Test
	public void defaultToString() {
		assertThat(pooledConnectionTimestamped.toString(),
				is(equalTo(ToStringBuilder
						.reflectionToString(pooledConnectionTimestamped))));
	}

	@Before
	public void setUp() {
		initMocks(this);
		pooledConnectionTimestamped = new PooledConnectionTimestamped(
				pooledConnection);
	}

}
