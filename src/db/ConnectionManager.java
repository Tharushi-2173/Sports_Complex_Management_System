package db;

import config.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String URL = ConfigLoader.getProperty("db.url", "jdbc:mysql://localhost:3306/sports_complex?useSSL=false&serverTimezone=UTC");
    private static final String USER = ConfigLoader.getProperty("db.user", "root");
    private static final String PASSWORD = ConfigLoader.getProperty("db.password", "");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Add mysql-connector-j to classpath.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}


