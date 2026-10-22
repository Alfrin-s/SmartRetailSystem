package dao;

import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * BaseDAO provides centralized connection management and resource cleanup.
 * Demonstrates Abstraction and Inheritance across the persistence layer.
 */
public abstract class BaseDAO {

    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    protected void close(AutoCloseable... resources) {
        DatabaseConnection.closeQuietly(resources);
    }
}
