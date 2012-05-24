package net.danieljurado.connectionpool.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TSPooledConnection implements PooledConnection,
		Comparable<TSPooledConnection> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DateTime timestamp = new DateTime();
	private final PooledConnection pooledConnection;

	TSPooledConnection(PooledConnection pooledConnection) {
		logger.info("Nova instancia");
		this.pooledConnection = pooledConnection;
	}

	@Override
	public void addConnectionEventListener(ConnectionEventListener listener) {
		pooledConnection.addConnectionEventListener(listener);
	}

	@Override
	public void addStatementEventListener(StatementEventListener listener) {
		pooledConnection.addStatementEventListener(listener);
	}

	@Override
	public void close() throws SQLException {
		pooledConnection.close();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return pooledConnection.getConnection();
	}

	@Override
	public void removeConnectionEventListener(ConnectionEventListener listener) {
		pooledConnection.removeConnectionEventListener(listener);
	}

	@Override
	public void removeStatementEventListener(StatementEventListener listener) {
		pooledConnection.removeStatementEventListener(listener);
	}

	@Override
	public int compareTo(TSPooledConnection o) {
		return o.timestamp.compareTo(timestamp);
	}

}