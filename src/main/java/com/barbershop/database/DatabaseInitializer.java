package com.barbershop.database;

import com.barbershop.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PASSWORD = "admin";

    private DatabaseInitializer() { }

    public static void initialize() {
        createSchema();
        seedUsers();
    }

    public static void reset() {
        String[] drops = {
                "DROP TABLE IF EXISTS transactions",
                "DROP TABLE IF EXISTS appointments",
                "DROP TABLE IF EXISTS services",
                "DROP TABLE IF EXISTS barbers",
                "DROP TABLE IF EXISTS customers",
                "DROP TABLE IF EXISTS users"
        };
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            for (String drop : drops) {
                statement.execute(drop);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not reset the database.", e);
        }
        initialize();
    }

    private static void createSchema() {
        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS users (
                    user_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    username      TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    full_name     TEXT NOT NULL,
                    role          TEXT NOT NULL DEFAULT 'Staff'
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name        TEXT NOT NULL,
                    phone       TEXT,
                    email       TEXT
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS barbers (
                    barber_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    name           TEXT NOT NULL,
                    specialization TEXT,
                    status         TEXT NOT NULL DEFAULT 'Available'
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS services (
                    service_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name       TEXT NOT NULL,
                    price      REAL NOT NULL DEFAULT 0,
                    duration   INTEGER NOT NULL DEFAULT 30
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS appointments (
                    appointment_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id      INTEGER NOT NULL,
                    barber_id        INTEGER NOT NULL,
                    service_id       INTEGER NOT NULL,
                    appointment_date TEXT NOT NULL,
                    appointment_time TEXT NOT NULL,
                    status           TEXT NOT NULL DEFAULT 'Booked',
                    FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
                    FOREIGN KEY (barber_id)   REFERENCES barbers (barber_id),
                    FOREIGN KEY (service_id)  REFERENCES services (service_id)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    appointment_id   INTEGER NOT NULL,
                    amount           REAL NOT NULL,
                    payment_method   TEXT NOT NULL,
                    transaction_date TEXT NOT NULL,
                    FOREIGN KEY (appointment_id) REFERENCES appointments (appointment_id)
                )
                """,
                "CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments (appointment_date)",
                "CREATE INDEX IF NOT EXISTS idx_appointments_customer ON appointments (customer_id)",
                "CREATE INDEX IF NOT EXISTS idx_customers_name ON customers (name)"
        };

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not create the database schema.", e);
        }
    }

    private static void seedUsers() {
        if (countRows("users") > 0) {
            return;
        }
        String sql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, DEFAULT_USERNAME);
            statement.setString(2, PasswordUtil.hash(DEFAULT_PASSWORD));
            statement.setString(3, "Shop Administrator");
            statement.setString(4, "Admin");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not create the default login.", e);
        }
    }

    private static int countRows(String table) {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not count rows in " + table + ".", e);
        }
    }
}
