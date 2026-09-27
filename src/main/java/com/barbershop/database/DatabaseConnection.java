package com.barbershop.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:sqlite:barbershop.db";

    private static String url = System.getProperty("barbershop.db.url", DEFAULT_URL);

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "SQLite JDBC driver not found. Make sure org.xerial:sqlite-jdbc is on the classpath.", e);
        }
    }

    private DatabaseConnection() { }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String newUrl) {
        url = newUrl;
    }

    public static void resetUrl() {
        url = System.getProperty("barbershop.db.url", DEFAULT_URL);
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }
}
