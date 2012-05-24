package net.danieljurado.connectionpool.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.danieljurado.connectionpool.ConnectionPoolManager;
import net.danieljurado.engine.Engine;
import net.danieljurado.engine.EngineFactory;

import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class ConnectionPoolManagerImpl implements ConnectionPoolManager,
		ConnectionEventListener, Runnable {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ConnectionPoolDataSource dataSource;
	private final Engine engine;

	private final Queue<PooledConnection> queue = new PriorityQueue<PooledConnection>();
	private final Semaphore semaphore = new Semaphore(10, true);

	@Inject
	ConnectionPoolManagerImpl(ConnectionPoolDataSource dataSource,
			EngineFactory engineFactory) {
		this.dataSource = dataSource;
		this.engine = engineFactory.create(this, Period.seconds(5), true);
	}

	@Override
	public void dispose() {
		engine.shutdown();
	}

	@Override
	public Connection getConnection() {

		try {
			if (!semaphore.tryAcquire(10, TimeUnit.SECONDS))
				throw new RuntimeException("timeout");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		boolean ok = false;
		try {
			synchronized (this) {
				PooledConnection pooledConnection;
				try {
					if (queue.size() > 0) {
						pooledConnection = queue.poll();
						logger.info("Obtive connection do pool: {}",
								pooledConnection);
					} else {
						pooledConnection = new TSPooledConnection(
								dataSource.getPooledConnection());
						logger.info("Obtive connection do datasource: {}",
								pooledConnection);
					}

					Connection connection = pooledConnection.getConnection();
					pooledConnection.addConnectionEventListener(this);
					ok = true;
					return connection;
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		} finally {
			if (!ok)
				semaphore.release();
		}
	}

	@Override
	public void connectionClosed(ConnectionEvent event) {
		PooledConnection pooledConnection = (PooledConnection) event
				.getSource();
		synchronized (this) {
			logger.info("Devolvendo conexao ao pool: {}", pooledConnection);
			pooledConnection.removeConnectionEventListener(this);
			queue.offer(pooledConnection);
			semaphore.release();
		}
	}

	@Override
	public void connectionErrorOccurred(ConnectionEvent event) {
		PooledConnection pooledConnection = (PooledConnection) event
				.getSource();
		logger.warn("Encerrando conexao com erro: {}", pooledConnection);
		try {
			pooledConnection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		logger.info("Connecions no pool: {}", queue.size());
	}

}
