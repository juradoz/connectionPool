package net.danieljurado.connectionpool.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EmptyStackException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.danieljurado.connectionpool.ConnectionPoolManager;
import net.danieljurado.connectionpool.impl.ConnectionPoolModule.MaxConnections;
import net.danieljurado.connectionpool.impl.ConnectionPoolModule.MaxIdleConnectionLife;
import net.danieljurado.connectionpool.impl.ConnectionPoolModule.Timeout;
import net.danieljurado.engine.Engine;
import net.danieljurado.engine.EngineFactory;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class ConnectionPoolManagerImpl implements ConnectionPoolManager, Runnable,
		ConnectionEventListener {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Engine engine;
	private final ConnectionPoolDataSource dataSource;
	private final int maxConnections;
	private final Semaphore semaphore;
	private final Period timeout;
	private final Period maxIdleConnectionLife;
	private final Queue<PooledConnectionTimestamped> recycledConnections = new PriorityQueue<PooledConnectionTimestamped>();

	private boolean isDisposed;
	private int activeConnections;

	@Inject
	ConnectionPoolManagerImpl(EngineFactory engineFactory,
			ConnectionPoolDataSource dataSource,
			@MaxConnections int maxConnections, @Timeout Period timeout,
			@MaxIdleConnectionLife Period maxIdleConnectionLife) {
		this.dataSource = dataSource;
		this.maxConnections = maxConnections;
		semaphore = new Semaphore(maxConnections, true);
		this.timeout = timeout;
		this.maxIdleConnectionLife = maxIdleConnectionLife;
		engine = engineFactory.create(this, timeout, true);
	}

	void assertInnerState() {
		if (isDisposed)
			return;
		if (activeConnections < 0)
			throw new AssertionError();
		if (activeConnections + recycledConnections.size() > maxConnections)
			throw new AssertionError();
		if (activeConnections + semaphore.availablePermits() > maxConnections)
			throw new AssertionError();
	}

	void closeConnectionNoEx(PooledConnection pconn) {
		try {
			pconn.close();
		} catch (SQLException e) {
			logger.error("Error while closing database connection: {}",
					e.getMessage());
		}
	}

	@Override
	public void connectionClosed(ConnectionEvent event) {
		PooledConnection pconn = (PooledConnection) event.getSource();
		logger.debug("pconn closed: {}", pconn);
		pconn.removeConnectionEventListener(this);
		recycleConnection(pconn);
	}

	@Override
	public void connectionErrorOccurred(ConnectionEvent event) {
		PooledConnection pconn = (PooledConnection) event.getSource();
		pconn.removeConnectionEventListener(this);
		disposeConnection(pconn);
	}

	@Override
	public void dispose() {
		if (isDisposed)
			return;
		logger.debug("disposing connectionpool...");
		isDisposed = true;
		engine.shutdown();
		while (!recycledConnections.isEmpty()) {
			PooledConnectionTimestamped pooledConnectionTimestamped = recycledConnections
					.poll();
			if (pooledConnectionTimestamped == null)
				throw new EmptyStackException();
			PooledConnection pconn = pooledConnectionTimestamped
					.getPooledConnection();
			disposeConnection(pconn);
		}
		logger.warn("connectionpool disposed");
	}

	synchronized void disposeConnection(PooledConnection pconn) {
		if (activeConnections < 0)
			throw new AssertionError();
		logger.debug("disposing pconn: {}", pconn);
		activeConnections--;
		semaphore.release();
		closeConnectionNoEx(pconn);
		assertInnerState();
	}

	@Override
	public Connection getConnection() {
		synchronized (this) {
			if (isDisposed)
				throw new IllegalStateException(
						"Connection pool has been disposed.");
		}
		try {
			if (!semaphore.tryAcquire(timeout.getSeconds(), TimeUnit.SECONDS))
				throw new RuntimeException(new TimeoutException());
		} catch (InterruptedException e) {
			throw new RuntimeException(
					"Interrupted while waiting for a database connection.", e);
		}
		boolean ok = false;
		try {
			Connection conn = getConnection2();
			ok = true;
			return conn;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (!ok)
				semaphore.release();
		}
	}

	synchronized Connection getConnection2() throws SQLException {
		if (isDisposed)
			throw new IllegalStateException(
					"Connection pool has been disposed.");
		PooledConnection pconn;
		if (recycledConnections.size() > 0) {
			logger.debug("retrieving connection from pool...");
			PooledConnectionTimestamped pooledConnectionTimestamped = recycledConnections
					.poll();
			if (pooledConnectionTimestamped == null)
				throw new EmptyStackException();
			pconn = pooledConnectionTimestamped.getPooledConnection();
			logger.debug("connection retrieved from pool: {}", pconn);
		} else {
			logger.debug("retrieving connection from datasource...");
			pconn = dataSource.getPooledConnection();
			logger.debug("connection retrieved from datasource: {}", pconn);
		}
		Connection conn = pconn.getConnection();
		activeConnections++;
		pconn.addConnectionEventListener(this);
		assertInnerState();
		return conn;
	}

	synchronized void recycleConnection(PooledConnection pconn) {
		if (isDisposed) {
			disposeConnection(pconn);
			return;
		}
		if (activeConnections <= 0)
			throw new AssertionError();
		logger.debug("recycling pcon to pool: {}", pconn);
		activeConnections--;
		semaphore.release();
		recycledConnections.add(new PooledConnectionTimestamped(pconn));
		assertInnerState();
	}

	@Override
	public void run() {
		DateTime now = new DateTime();
		synchronized (this) {
			for (PooledConnectionTimestamped pooledConnectionTimestamped : recycledConnections) {
				Period period = new Period(
						pooledConnectionTimestamped.getTimestamp(), now);
				if (period.toDurationFrom(now).isLongerThan(
						maxIdleConnectionLife.toDurationFrom(now))) {
					closeConnectionNoEx(pooledConnectionTimestamped
							.getPooledConnection());
					recycledConnections.remove(pooledConnectionTimestamped);
				}
			}
		}
	}

}
