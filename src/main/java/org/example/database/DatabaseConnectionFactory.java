package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionFactory {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseConnectionFactory(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}