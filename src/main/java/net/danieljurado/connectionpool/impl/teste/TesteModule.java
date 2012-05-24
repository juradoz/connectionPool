package net.danieljurado.connectionpool.impl.teste;

import javax.sql.ConnectionPoolDataSource;

import com.google.inject.AbstractModule;

public class TesteModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConnectionPoolDataSource.class).toProvider(
				ConnectionPoolDataSourceProvider.class);
	}

}
