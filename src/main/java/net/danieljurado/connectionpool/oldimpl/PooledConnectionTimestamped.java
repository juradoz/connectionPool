package net.danieljurado.connectionpool.oldimpl;

import javax.sql.PooledConnection;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PooledConnectionTimestamped implements
		Comparable<PooledConnectionTimestamped> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final PooledConnection pooledConnection;
	private final DateTime timestamp = new DateTime();

	PooledConnectionTimestamped(PooledConnection pooledConnection) {
		logger.debug("Nova instancia");
		this.pooledConnection = pooledConnection;
	}

	@Override
	public int compareTo(PooledConnectionTimestamped other) {
		return new CompareToBuilder().append(other.timestamp, timestamp)
				.toComparison();
	}

	public PooledConnection getPooledConnection() {
		return pooledConnection;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
