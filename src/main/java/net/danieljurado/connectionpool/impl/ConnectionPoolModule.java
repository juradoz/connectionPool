package net.danieljurado.connectionpool.impl;

import javax.sql.ConnectionPoolDataSource;

import org.joda.time.Period;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConnectionPoolModule extends AbstractModule {

	private final ConnectionPoolDataSource dataSource;
	private final int maxConnections;
	private final Period timeout;
	private final Period maxIdleConnectionLife;

	public ConnectionPoolModule(ConnectionPoolDataSource dataSource,
			int maxConnections, Period timeout, Period maxIdleConnectionLife) {
		this.dataSource = dataSource;
		this.maxConnections = maxConnections;
		this.timeout = timeout;
		this.maxIdleConnectionLife = maxIdleConnectionLife;
	}

	@Override
	protected void configure() {
		bind(ConnectionPoolDataSource.class).toInstance(dataSource);
		bindConstant().annotatedWith(Names.named("maxConnections")).to(
				maxConnections);
		bind(Period.class).annotatedWith(Names.named("timeout")).toInstance(
				timeout);
		bind(Period.class).annotatedWith(Names.named("maxIdleConnectionLife"))
				.toInstance(maxIdleConnectionLife);
	}

}
