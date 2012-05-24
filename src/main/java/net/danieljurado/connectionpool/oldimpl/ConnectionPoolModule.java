package net.danieljurado.connectionpool.oldimpl;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.danieljurado.connectionpool.ConnectionPoolManager;

import org.joda.time.Period;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;

class ConnectionPoolModule extends AbstractModule {

	@Retention(RUNTIME)
	@Target({ PARAMETER })
	@BindingAnnotation
	public @interface MaxConnections {
	}

	@Retention(RUNTIME)
	@Target({ PARAMETER })
	@BindingAnnotation
	public @interface MaxIdleConnectionLife {
	}

	@Retention(RUNTIME)
	@Target({ PARAMETER })
	@BindingAnnotation
	public @interface Timeout {
	}

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
		bindConstant().annotatedWith(MaxConnections.class).to(maxConnections);
		bind(Period.class).annotatedWith(Timeout.class).toInstance(timeout);
		bind(Period.class).annotatedWith(MaxIdleConnectionLife.class)
				.toInstance(maxIdleConnectionLife);
		bind(ConnectionPoolManager.class).to(ConnectionPoolManagerImpl.class);
	}

}
