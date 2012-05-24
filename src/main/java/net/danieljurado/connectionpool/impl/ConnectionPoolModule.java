package net.danieljurado.connectionpool.impl;

import net.danieljurado.connectionpool.ConnectionPoolManager;

import com.google.inject.AbstractModule;

public class ConnectionPoolModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConnectionPoolManager.class).to(ConnectionPoolManagerImpl.class);
	}

}
