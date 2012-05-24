package net.danieljurado.connectionpool.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.danieljurado.connectionpool.ConnectionPoolManager;
import net.danieljurado.connectionpool.impl.ConnectionPoolModule.AcquireTimeout;
import net.danieljurado.connectionpool.impl.ConnectionPoolModule.MaxConnections;
import net.danieljurado.connectionpool.impl.ConnectionPoolModule.MaxIdleConnectionLife;
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
	private final Period maxIdleConnectionLife;
	private final Period acquireTimeout;

	private final Stack<TSPooledConnection> stack = new Stack<TSPooledConnection>();
	private final Semaphore semaphore;

	@Inject
	ConnectionPoolManagerImpl(ConnectionPoolDataSource dataSource,
			EngineFactory engineFactory, @MaxConnections int maxConnections,
			@MaxIdleConnectionLife Period maxIdleConnectionLife,
			@AcquireTimeout Period acquireTimeout) {
		this.dataSource = dataSource;
		this.engine = engineFactory.create(this, Period.seconds(5), true);
		this.semaphore = new Semaphore(maxConnections, true);
		this.maxIdleConnectionLife = maxIdleConnectionLife;
		this.acquireTimeout = acquireTimeout;
	}

	@Override
	public void dispose() {
		engine.shutdown();
	}

	@Override
	public Connection getConnection() {

		try {
			if (!semaphore.tryAcquire(acquireTimeout.getSeconds(),
					TimeUnit.SECONDS))
				throw new RuntimeException("timeout");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		boolean ok = false;
		try {
			synchronized (this) {
				PooledConnection pooledConnection;
				try {
					if (!stack.empty()) {
						pooledConnection = stack.pop();
						logger.debug("Obtive connection do pool: {}",
								pooledConnection);
					} else {
						pooledConnection = dataSource.getPooledConnection();
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
			logger.debug("Devolvendo conexao ao pool: {}", pooledConnection);
			pooledConnection.removeConnectionEventListener(this);
			stack.push(new TSPooledConnection(pooledConnection));
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
		logger.debug("Connecions no pool: {}", stack.size());
		synchronized (this) {
			for (Iterator<TSPooledConnection> iterator = stack.iterator(); iterator
					.hasNext();) {
				TSPooledConnection tsPooledConnection = iterator.next();
				if (tsPooledConnection.getTimestamp()
						.plus(maxIdleConnectionLife).isAfterNow())
					continue;
				logger.warn("Removendo connection por inatividade: {}",
						tsPooledConnection);
				iterator.remove();
			}
		}
	}

}
