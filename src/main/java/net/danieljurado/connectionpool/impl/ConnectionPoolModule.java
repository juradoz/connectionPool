package net.danieljurado.connectionpool.impl;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.danieljurado.connectionpool.ConnectionPoolManager;

import org.joda.time.Period;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;

public class ConnectionPoolModule extends AbstractModule {

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
	@interface AcquireTimeout {
	}

	@Override
	protected void configure() {
		bindConstant().annotatedWith(MaxConnections.class).to(10);
		bind(Period.class).annotatedWith(MaxIdleConnectionLife.class)
				.toInstance(Period.minutes(1));
		bind(Period.class).annotatedWith(AcquireTimeout.class).toInstance(
				Period.seconds(10));
		bind(ConnectionPoolManager.class).to(ConnectionPoolManagerImpl.class);
	}

}
