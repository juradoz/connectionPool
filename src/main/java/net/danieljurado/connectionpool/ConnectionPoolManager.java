package net.danieljurado.connectionpool;

import java.sql.Connection;

public interface ConnectionPoolManager {

	void dispose();

	Connection getConnection();
}
