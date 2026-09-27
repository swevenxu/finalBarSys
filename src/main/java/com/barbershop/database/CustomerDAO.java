package com.barbershop.database;

import com.barbershop.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO {
    public List<Customer> findAll() {
        String sql = "SELECT customer_id, name, phone, email FROM customers ORDER BY name COLLATE NOCASE";
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                customers.add(map(rs));
            }
            return customers;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load customers.", e);
        }
    }

    public Optional<Customer> findById(int customerId) {
        String sql = "SELECT customer_id, name, phone, email FROM customers WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load customer #" + customerId + ".", e);
        }
    }

    public int insert(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getName());
            statement.setString(2, customer.getPhone());
            statement.setString(3, customer.getEmail());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    customer.setCustomerId(id);
                    return id;
                }
            }
            throw new DataAccessException("Inserting the customer returned no id.");
        } catch (SQLException e) {
            throw new DataAccessException("Could not save customer \"" + customer.getName() + "\".", e);
        }
    }

    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, email = ? WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getName());
            statement.setString(2, customer.getPhone());
            statement.setString(3, customer.getEmail());
            statement.setInt(4, customer.getCustomerId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update customer \"" + customer.getName() + "\".", e);
        }
    }

    public boolean delete(int customerId) {
        String deleteTransactions = """
                DELETE FROM transactions WHERE appointment_id IN
                    (SELECT appointment_id FROM appointments WHERE customer_id = ?)
                """;
        String deleteAppointments = "DELETE FROM appointments WHERE customer_id = ?";
        String deleteCustomer = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement t = connection.prepareStatement(deleteTransactions);
                 PreparedStatement a = connection.prepareStatement(deleteAppointments);
                 PreparedStatement c = connection.prepareStatement(deleteCustomer)) {
                t.setInt(1, customerId);
                t.executeUpdate();

                a.setInt(1, customerId);
                a.executeUpdate();

                c.setInt(1, customerId);
                boolean deleted = c.executeUpdate() > 0;

                connection.commit();
                return deleted;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete customer #" + customerId + ".", e);
        }
    }

    public int countAppointments(int customerId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not count appointments.", e);
        }
    }

    public double totalSpent(int customerId) {
        String sql = """
                SELECT COALESCE(SUM(t.amount), 0)
                FROM transactions t
                JOIN appointments a ON a.appointment_id = t.appointment_id
                WHERE a.customer_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not total customer spending.", e);
        }
    }

    public boolean phoneExists(String phone, int excludingCustomerId) {
        String sql = "SELECT COUNT(*) FROM customers WHERE phone = ? AND customer_id <> ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phone);
            statement.setInt(2, excludingCustomerId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not check the phone number.", e);
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"));
    }
}
