package com.barbershop.database;

import com.barbershop.model.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentDAO {
    private static final String SELECT_WITH_DETAILS = """
            SELECT a.appointment_id, a.customer_id, a.barber_id, a.service_id,
                   a.appointment_date, a.appointment_time, a.status,
                   c.name AS customer_name,
                   b.name AS barber_name,
                   s.name AS service_name,
                   s.price AS service_price
            FROM appointments a
            JOIN customers c ON c.customer_id = a.customer_id
            JOIN barbers   b ON b.barber_id   = a.barber_id
            JOIN services  s ON s.service_id  = a.service_id
            """;

    public List<Appointment> findAllWithDetails() {
        return queryList(SELECT_WITH_DETAILS + " ORDER BY a.appointment_date, a.appointment_time");
    }

    public List<Appointment> findByDate(LocalDate date) {
        String sql = SELECT_WITH_DETAILS + " WHERE a.appointment_date = ? ORDER BY a.appointment_time";
        List<Appointment> results = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, date.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load appointments for " + date + ".", e);
        }
    }

    public List<Appointment> findByCustomer(int customerId) {
        String sql = SELECT_WITH_DETAILS
                + " WHERE a.customer_id = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        List<Appointment> results = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load the appointment history.", e);
        }
    }

    public Optional<Appointment> findByIdWithDetails(int appointmentId) {
        String sql = SELECT_WITH_DETAILS + " WHERE a.appointment_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load appointment #" + appointmentId + ".", e);
        }
    }

    public int insert(Appointment appointment) {
        String sql = """
                INSERT INTO appointments
                    (customer_id, barber_id, service_id, appointment_date, appointment_time, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindAppointment(statement, appointment);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    appointment.setAppointmentId(id);
                    return id;
                }
            }
            throw new DataAccessException("Inserting the appointment returned no id.");
        } catch (SQLException e) {
            throw new DataAccessException("Could not save the appointment.", e);
        }
    }

    public boolean update(Appointment appointment) {
        String sql = """
                UPDATE appointments
                   SET customer_id = ?, barber_id = ?, service_id = ?,
                       appointment_date = ?, appointment_time = ?, status = ?
                 WHERE appointment_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindAppointment(statement, appointment);
            statement.setInt(7, appointment.getAppointmentId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update the appointment.", e);
        }
    }

    public boolean updateStatus(int appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, appointmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update the appointment status.", e);
        }
    }

    public boolean delete(int appointmentId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement deleteTransactions =
                         connection.prepareStatement("DELETE FROM transactions WHERE appointment_id = ?");
                 PreparedStatement deleteAppointment =
                         connection.prepareStatement("DELETE FROM appointments WHERE appointment_id = ?")) {
                deleteTransactions.setInt(1, appointmentId);
                deleteTransactions.executeUpdate();

                deleteAppointment.setInt(1, appointmentId);
                boolean deleted = deleteAppointment.executeUpdate() > 0;

                connection.commit();
                return deleted;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete appointment #" + appointmentId + ".", e);
        }
    }

    public int countByDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, date.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not count today's appointments.", e);
        }
    }

    public int countByDateAndStatus(LocalDate date, String status) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = ? AND status = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, date.toString());
            statement.setString(2, status);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not count appointments with status " + status + ".", e);
        }
    }

    private List<Appointment> queryList(String sql) {
        List<Appointment> results = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                results.add(map(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load appointments.", e);
        }
    }

    private void bindAppointment(PreparedStatement statement, Appointment appointment) throws SQLException {
        statement.setInt(1, appointment.getCustomerId());
        statement.setInt(2, appointment.getBarberId());
        statement.setInt(3, appointment.getServiceId());
        statement.setString(4, appointment.getAppointmentDate().toString());
        statement.setString(5, appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        statement.setString(6, appointment.getStatus());
    }

    private Appointment map(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment(
                rs.getInt("appointment_id"),
                rs.getInt("customer_id"),
                rs.getInt("barber_id"),
                rs.getInt("service_id"),
                LocalDate.parse(rs.getString("appointment_date")),
                LocalTime.parse(rs.getString("appointment_time")),
                rs.getString("status"));
        appointment.setCustomerName(rs.getString("customer_name"));
        appointment.setBarberName(rs.getString("barber_name"));
        appointment.setServiceName(rs.getString("service_name"));
        appointment.setServicePrice(rs.getDouble("service_price"));
        return appointment;
    }
}
