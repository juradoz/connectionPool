package net.danieljurado.connectionpool.impl.teste;

import javax.sql.ConnectionPoolDataSource;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

@Singleton
class ConnectionPoolDataSourceProvider implements
		Provider<ConnectionPoolDataSource> {

	private final MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();

	ConnectionPoolDataSourceProvider() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		dataSource.setURL("jdbc:mysql://localhost:3306/oi");
		dataSource.setUser("root");
		dataSource.setPassword("root");
	}

	@Override
	public ConnectionPoolDataSource get() {
		return dataSource;
	}

}
