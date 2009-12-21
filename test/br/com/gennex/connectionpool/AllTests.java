package br.com.gennex.connectionpool;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { IntegerGreaterThanZeroTest.class,
		MaxConnectionsTest.class, MaxIdleConnectionLifeTest.class,
		TimeoutTest.class })
public class AllTests {

}
