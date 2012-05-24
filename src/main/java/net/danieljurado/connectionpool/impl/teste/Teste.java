package net.danieljurado.connectionpool.impl.teste;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.danieljurado.connectionpool.ConnectionPoolManager;
import net.danieljurado.connectionpool.impl.ConnectionPoolModule;
import net.danieljurado.engine.impl.EngineModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;

public class Teste implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ConnectionPoolManager connectionPoolManager;

	private Teste(ConnectionPoolManager connectionPoolManager) {
		this.connectionPoolManager = connectionPoolManager;
	}

	public static void main(String[] args) throws InterruptedException {
		ConnectionPoolManager connectionPoolManager = Guice.createInjector(
				new EngineModule(), new TesteModule(),
				new ConnectionPoolModule()).getInstance(
				ConnectionPoolManager.class);

		for (int i = 0; i < 50; i++)
			new Thread(new Teste(connectionPoolManager)).start();

		while (true)
			Thread.sleep(10);
	}

	@Override
	public void run() {
		while (Thread.currentThread().isAlive()) {
			Connection connection = connectionPoolManager.getConnection();
			try {
				PreparedStatement preparedStatement = connection
						.prepareStatement("select Now() as dataHora");
				ResultSet resultSet = preparedStatement.executeQuery();
				resultSet.next();
				logger.info(resultSet.getString("dataHora"));
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					connection.close();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
