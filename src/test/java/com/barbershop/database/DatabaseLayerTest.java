package com.barbershop.database;

import com.barbershop.model.Appointment;
import com.barbershop.model.Barber;
import com.barbershop.model.Customer;
import com.barbershop.model.Service;
import com.barbershop.model.Transaction;
import com.barbershop.model.User;
import com.barbershop.util.PasswordUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseLayerTest {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BarberDAO barberDAO = new BarberDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final UserDAO userDAO = new UserDAO();

    @BeforeAll
    static void pointAtTestDatabase() {
        DatabaseConnection.setUrl("jdbc:sqlite:target/test-barbershop.db");
    }

    @BeforeEach
    void resetDatabase() {
        DatabaseInitializer.reset();
    }

    @Test
    @DisplayName("the schema is created and the default login is seeded")
    void seedsDefaultLogin() {
        Optional<User> admin = userDAO.authenticate(
                DatabaseInitializer.DEFAULT_USERNAME,
                PasswordUtil.hash(DatabaseInitializer.DEFAULT_PASSWORD));

        assertTrue(admin.isPresent(), "admin/admin should be able to sign in");
        assertEquals("Admin", admin.get().getRole());

        assertTrue(userDAO.authenticate("admin", PasswordUtil.hash("wrong-password")).isEmpty(),
                "a wrong password must not authenticate");
    }

    @Test
    @DisplayName("customers can be created, read, updated and deleted")
    void customerCrud() {
        int id = customerDAO.insert(new Customer("Test Customer", "0999-000-0001", "test@example.com"));
        assertTrue(id > 0);

        Optional<Customer> loaded = customerDAO.findById(id);
        assertTrue(loaded.isPresent());
        assertEquals("Test Customer", loaded.get().getName());

        Customer customer = loaded.get();
        customer.setName("Renamed Customer");
        assertTrue(customerDAO.update(customer));
        assertEquals("Renamed Customer", customerDAO.findById(id).orElseThrow().getName());

        assertTrue(customerDAO.phoneExists("0999-000-0001", 0));
        assertTrue(customerDAO.delete(id));
        assertTrue(customerDAO.findById(id).isEmpty());
    }

    @Test
    @DisplayName("barbers and services can be managed")
    void barberAndServiceCrud() {
        int barberId = barberDAO.insert(new Barber("Test Barber", "Fades", Barber.AVAILABLE));
        assertTrue(barberId > 0);
        assertTrue(barberDAO.updateStatus(barberId, Barber.BUSY));
        assertEquals(Barber.BUSY, barberDAO.findById(barberId).orElseThrow().getStatus());
        assertFalse(barberDAO.findByStatus(Barber.AVAILABLE).stream()
                .anyMatch(barber -> barber.getBarberId() == barberId));

        int serviceId = serviceDAO.insert(new Service("Test Service", 175.50, 25));
        Service service = serviceDAO.findById(serviceId).orElseThrow();
        assertEquals(175.50, service.getPrice(), 0.001);
        assertEquals(25, service.getDurationMinutes());
        assertTrue(serviceDAO.delete(serviceId));
    }

    @Test
    @DisplayName("appointments join in the customer, barber and service names")
    void appointmentBookingWithDetails() {
        int customerId = customerDAO.insert(new Customer("Joiner Test", "0999-000-0002", null));
        int barberId = barberDAO.insert(new Barber("Joiner Barber", "Fades", Barber.AVAILABLE));
        int serviceId = serviceDAO.insert(new Service("Joiner Service", 175.50, 25));
        assertTrue(barberId > 0);
        assertTrue(serviceId > 0);

        LocalDate date = LocalDate.now().plusDays(2);
        Appointment appointment = new Appointment(
                customerId, barberId, serviceId,
                date, LocalTime.of(14, 30), Appointment.BOOKED);
        int appointmentId = appointmentDAO.insert(appointment);
        assertTrue(appointmentId > 0);

        Appointment loaded = appointmentDAO.findByIdWithDetails(appointmentId).orElseThrow();
        assertEquals("Joiner Test", loaded.getCustomerName());
        assertEquals("Joiner Barber", loaded.getBarberName());
        assertEquals("Joiner Service", loaded.getServiceName());
        assertEquals("14:30", loaded.getFormattedTime());
        assertEquals(175.50, loaded.getServicePrice(), 0.001);

        assertTrue(appointmentDAO.countByDate(date) >= 1);
        assertTrue(appointmentDAO.updateStatus(appointmentId, Appointment.DONE));
        assertEquals(1, appointmentDAO.countByDateAndStatus(date, Appointment.DONE));

        assertEquals(1, appointmentDAO.findByCustomer(customerId).size());
        assertTrue(appointmentDAO.delete(appointmentId));
    }

    @Test
    @DisplayName("payments are recorded, totalled and can be undone")
    void transactionRecording() {
        int customerId = customerDAO.insert(new Customer("Payer Test", "0999-000-0003", null));
        Service service = new Service("Payer Service", 120.0, 30);
        int serviceId = serviceDAO.insert(service);
        int barberId = barberDAO.insert(new Barber("Payer Barber", "Beards", Barber.AVAILABLE));

        Appointment appointment = new Appointment(
                customerId, barberId, serviceId,
                LocalDate.now(), LocalTime.of(9, 15), Appointment.DONE);
        int appointmentId = appointmentDAO.insert(appointment);

        Transaction transaction = new Transaction(appointmentId, service.getPrice(), Transaction.CASH);
        transaction.setTransactionDate(LocalDateTime.now());
        int transactionId = transactionDAO.insert(transaction);
        assertTrue(transactionId > 0);

        assertTrue(transactionDAO.findByAppointment(appointmentId).isPresent());
        assertTrue(transactionDAO.totalRevenueForDate(LocalDate.now()) >= service.getPrice());
        assertTrue(transactionDAO.countForDate(LocalDate.now()) >= 1);

        Transaction loaded = transactionDAO.findById(transactionId).orElseThrow();
        assertEquals("Payer Test", loaded.getCustomerName());
        assertEquals(service.getName(), loaded.getServiceName());

        assertTrue(transactionDAO.delete(transactionId));
        assertTrue(transactionDAO.findByAppointment(appointmentId).isEmpty());
    }

    @Test
    @DisplayName("deleting a customer cascades to their appointments and payments")
    void deletingCustomerRemovesRelatedRows() {
        int customerId = customerDAO.insert(new Customer("Cascade Test", "0999-000-0004", null));
        Service service = new Service("Cascade Service", 90.0, 20);
        int serviceId = serviceDAO.insert(service);
        int barberId = barberDAO.insert(new Barber("Cascade Barber", "Kids", Barber.AVAILABLE));

        int appointmentId = appointmentDAO.insert(new Appointment(
                customerId, barberId, serviceId,
                LocalDate.now(), LocalTime.of(16, 0), Appointment.DONE));
        transactionDAO.insert(new Transaction(appointmentId, service.getPrice(), Transaction.GCASH));

        assertTrue(customerDAO.delete(customerId));
        assertTrue(appointmentDAO.findByIdWithDetails(appointmentId).isEmpty(),
                "the appointment should have been removed with the customer");
        assertTrue(transactionDAO.findByAppointment(appointmentId).isEmpty(),
                "the payment should have been removed with the customer");
    }

    @Test
    @DisplayName("a fresh install seeds the service menu and the default admin login only")
    void freshInstallStartsClean() {
        assertEquals(19, serviceDAO.findAll().size(), "the service menu should be pre-seeded");
        assertTrue(serviceDAO.findAll().stream().noneMatch(s -> s.getName().equals("Basic Haircut")
                        && s.getPrice() != 150.0),
                "seeded prices should match the price list");
        assertTrue(barberDAO.findAll().isEmpty(), "no barbers should be pre-seeded");
        assertTrue(customerDAO.findAll().isEmpty(), "no customers should be pre-seeded");
        assertTrue(appointmentDAO.findByDate(LocalDate.now()).isEmpty(),
                "no appointments should be pre-seeded");

        assertTrue(userDAO.authenticate(
                        DatabaseInitializer.DEFAULT_USERNAME,
                        PasswordUtil.hash(DatabaseInitializer.DEFAULT_PASSWORD)).isPresent(),
                "only the default admin login is created");
    }
}
