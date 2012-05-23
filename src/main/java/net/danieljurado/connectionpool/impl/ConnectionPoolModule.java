package net.danieljurado.connectionpool.impl;

import net.danieljurado.connectionpool.ConnectionPoolManager;

import org.joda.time.Period;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConnectionPoolModule extends AbstractModule {

	private final int maxConnections;
	private final Period timeout;
	private final Period maxIdleConnectionLife;

	public ConnectionPoolModule() {
		this(60, Period.minutes(1), Period.minutes(1));
	}

	public ConnectionPoolModule(int maxConnections, Period timeout,
			Period maxIdleConnectionLife) {
		this.maxConnections = maxConnections;
		this.timeout = timeout;
		this.maxIdleConnectionLife = maxIdleConnectionLife;
	}

	@Override
	protected void configure() {
		bindConstant().annotatedWith(Names.named("maxConnections")).to(
				maxConnections);
		bind(Period.class).annotatedWith(Names.named("timeout")).toInstance(
				timeout);
		bind(Period.class).annotatedWith(Names.named("maxIdleConnectionLife"))
				.toInstance(maxIdleConnectionLife);
		bind(ConnectionPoolManager.class).to(ConnectionPoolManagerImpl.class);
	}

}
