package com.barbershop.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates JDBC connections to the SQLite database file.
 *
 * <p>SQLite needs no server, so the whole database is a single file next to the application.
 * The location can be overridden with the {@code barbershop.db.url} system property, which the
 * unit tests use to point at a temporary file.</p>
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:sqlite:barbershop.db";

    private static String url = System.getProperty("barbershop.db.url", DEFAULT_URL);

    static {
        try {
            // Registers the org.sqlite.JDBC driver explicitly; the SPI would normally do this,
            // but doing it here gives a much clearer error if the dependency is missing.
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "SQLite JDBC driver not found. Make sure org.xerial:sqlite-jdbc is on the classpath.", e);
        }
    }

    private DatabaseConnection() {
        // utility class
    }

    public static String getUrl() {
        return url;
    }

    /** Points the application at a different database file. Used by tests. */
    public static void setUrl(String newUrl) {
        url = newUrl;
    }

    /** Resets the URL back to the default {@code barbershop.db} file. */
    public static void resetUrl() {
        url = System.getProperty("barbershop.db.url", DEFAULT_URL);
    }

    /**
     * Opens a new connection. The caller is responsible for closing it, so always use
     * try-with-resources.
     *
     * <p>SQLite does not enforce foreign keys unless asked, so {@code PRAGMA foreign_keys = ON}
     * is issued on every new connection.</p>
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }
}
