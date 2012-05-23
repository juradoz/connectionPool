package net.danieljurado.connectionpool;

import java.sql.Connection;

public interface ConnectionPoolManager {
	
	Connection getConnection();
	
	void dispose();
}
