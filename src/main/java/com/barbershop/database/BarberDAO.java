package com.barbershop.database;

import com.barbershop.model.Barber;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BarberDAO {
    public List<Barber> findAll() {
        String sql = "SELECT barber_id, name, specialization, status FROM barbers ORDER BY name COLLATE NOCASE";
        List<Barber> barbers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                barbers.add(map(rs));
            }
            return barbers;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load barbers.", e);
        }
    }

    public List<Barber> findByStatus(String status) {
        String sql = "SELECT barber_id, name, specialization, status FROM barbers "
                + "WHERE status = ? ORDER BY name COLLATE NOCASE";
        List<Barber> barbers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    barbers.add(map(rs));
                }
            }
            return barbers;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load barbers with status " + status + ".", e);
        }
    }

    public Optional<Barber> findById(int barberId) {
        String sql = "SELECT barber_id, name, specialization, status FROM barbers WHERE barber_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, barberId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load barber #" + barberId + ".", e);
        }
    }

    public int insert(Barber barber) {
        String sql = "INSERT INTO barbers (name, specialization, status) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, barber.getName());
            statement.setString(2, barber.getSpecialization());
            statement.setString(3, barber.getStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    barber.setBarberId(id);
                    return id;
                }
            }
            throw new DataAccessException("Inserting the barber returned no id.");
        } catch (SQLException e) {
            throw new DataAccessException("Could not save barber \"" + barber.getName() + "\".", e);
        }
    }

    public boolean update(Barber barber) {
        String sql = "UPDATE barbers SET name = ?, specialization = ?, status = ? WHERE barber_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, barber.getName());
            statement.setString(2, barber.getSpecialization());
            statement.setString(3, barber.getStatus());
            statement.setInt(4, barber.getBarberId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update barber \"" + barber.getName() + "\".", e);
        }
    }

    public boolean updateStatus(int barberId, String status) {
        String sql = "UPDATE barbers SET status = ? WHERE barber_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, barberId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update the barber status.", e);
        }
    }

    public boolean delete(int barberId) {
        String sql = "DELETE FROM barbers WHERE barber_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, barberId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not delete barber #" + barberId
                            + ". A barber with existing appointments cannot be removed.", e);
        }
    }

    private Barber map(ResultSet rs) throws SQLException {
        return new Barber(
                rs.getInt("barber_id"),
                rs.getString("name"),
                rs.getString("specialization"),
                rs.getString("status"));
    }
}
