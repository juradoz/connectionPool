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
	public @interface MaxConnectionsParameter {
	}

	@Retention(RUNTIME)
	@Target({ PARAMETER })
	@BindingAnnotation
	public @interface MaxIdleConnectionLifeParameter {
	}

	@Retention(RUNTIME)
	@Target({ PARAMETER })
	@BindingAnnotation
	@interface AcquireTimeoutParameter {
	}

	@Override
	protected void configure() {
		bindConstant().annotatedWith(MaxConnectionsParameter.class).to(10);
		bind(Period.class).annotatedWith(MaxIdleConnectionLifeParameter.class)
				.toInstance(Period.minutes(1));
		bind(Period.class).annotatedWith(AcquireTimeoutParameter.class)
				.toInstance(Period.seconds(10));
		bind(ConnectionPoolManager.class).to(ConnectionPoolManagerImpl.class);
	}

}
