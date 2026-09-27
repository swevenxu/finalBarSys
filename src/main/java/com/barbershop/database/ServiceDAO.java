package com.barbershop.database;

import com.barbershop.model.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the {@code services} table.
 */
public class ServiceDAO {

    public List<Service> findAll() {
        String sql = "SELECT service_id, name, price, duration FROM services ORDER BY name COLLATE NOCASE";
        List<Service> services = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                services.add(map(rs));
            }
            return services;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load services.", e);
        }
    }

    public Optional<Service> findById(int serviceId) {
        String sql = "SELECT service_id, name, price, duration FROM services WHERE service_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load service #" + serviceId + ".", e);
        }
    }

    public int insert(Service service) {
        String sql = "INSERT INTO services (name, price, duration) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, service.getName());
            statement.setDouble(2, service.getPrice());
            statement.setInt(3, service.getDurationMinutes());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    service.setServiceId(id);
                    return id;
                }
            }
            throw new DataAccessException("Inserting the service returned no id.");
        } catch (SQLException e) {
            throw new DataAccessException("Could not save service \"" + service.getName() + "\".", e);
        }
    }

    public boolean update(Service service) {
        String sql = "UPDATE services SET name = ?, price = ?, duration = ? WHERE service_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, service.getName());
            statement.setDouble(2, service.getPrice());
            statement.setInt(3, service.getDurationMinutes());
            statement.setInt(4, service.getServiceId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update service \"" + service.getName() + "\".", e);
        }
    }

    public boolean delete(int serviceId) {
        String sql = "DELETE FROM services WHERE service_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not delete service #" + serviceId
                            + ". A service used by an existing appointment cannot be removed.", e);
        }
    }

    private Service map(ResultSet rs) throws SQLException {
        return new Service(
                rs.getInt("service_id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getInt("duration"));
    }
}
