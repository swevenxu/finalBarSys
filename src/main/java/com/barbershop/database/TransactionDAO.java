package com.barbershop.database;

import com.barbershop.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the {@code transactions} table.
 */
public class TransactionDAO {

    private static final DateTimeFormatter STORED_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SELECT_WITH_DETAILS = """
            SELECT t.transaction_id, t.appointment_id, t.amount, t.payment_method, t.transaction_date,
                   c.name AS customer_name,
                   s.name AS service_name
            FROM transactions t
            JOIN appointments a ON a.appointment_id = t.appointment_id
            JOIN customers    c ON c.customer_id    = a.customer_id
            JOIN services     s ON s.service_id     = a.service_id
            """;

    /** Every payment, newest first. */
    public List<Transaction> findAllWithDetails() {
        String sql = SELECT_WITH_DETAILS + " ORDER BY t.transaction_date DESC, t.transaction_id DESC";
        List<Transaction> results = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                results.add(map(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load transactions.", e);
        }
    }

    /** The most recent {@code limit} payments, newest first. */
    public List<Transaction> findRecent(int limit) {
        String sql = SELECT_WITH_DETAILS + " ORDER BY t.transaction_date DESC, t.transaction_id DESC LIMIT ?";
        List<Transaction> results = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load recent transactions.", e);
        }
    }

    public Optional<Transaction> findById(int transactionId) {
        String sql = SELECT_WITH_DETAILS + " WHERE t.transaction_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transactionId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load transaction #" + transactionId + ".", e);
        }
    }

    /** Whether a payment has already been recorded for this appointment. */
    public Optional<Transaction> findByAppointment(int appointmentId) {
        String sql = SELECT_WITH_DETAILS + " WHERE t.appointment_id = ? LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load the appointment's payment.", e);
        }
    }

    public int insert(Transaction transaction) {
        String sql = """
                INSERT INTO transactions (appointment_id, amount, payment_method, transaction_date)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, transaction.getAppointmentId());
            statement.setDouble(2, transaction.getAmount());
            statement.setString(3, transaction.getPaymentMethod());
            statement.setString(4, transaction.getTransactionDate().format(STORED_FORMAT));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    transaction.setTransactionId(id);
                    return id;
                }
            }
            throw new DataAccessException("Inserting the payment returned no id.");
        } catch (SQLException e) {
            throw new DataAccessException("Could not record the payment.", e);
        }
    }

    public boolean delete(int transactionId) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transactionId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not undo transaction #" + transactionId + ".", e);
        }
    }

    /** Today's revenue, used by the dashboard. */
    public double totalRevenueForDate(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE DATE(transaction_date) = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, date.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not total today's revenue.", e);
        }
    }

    /** All-time revenue. */
    public double totalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not total revenue.", e);
        }
    }

    /** Number of payments recorded on a date — the "customers served" dashboard figure. */
    public int countForDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE DATE(transaction_date) = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, date.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not count today's payments.", e);
        }
    }

    private Transaction map(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("appointment_id"),
                rs.getDouble("amount"),
                rs.getString("payment_method"),
                LocalDateTime.parse(rs.getString("transaction_date"), STORED_FORMAT));
        transaction.setCustomerName(rs.getString("customer_name"));
        transaction.setServiceName(rs.getString("service_name"));
        return transaction;
    }
}
