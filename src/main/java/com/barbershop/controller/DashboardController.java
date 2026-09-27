package com.barbershop.controller;

import com.barbershop.database.AppointmentDAO;
import com.barbershop.database.DataAccessException;
import com.barbershop.database.TransactionDAO;
import com.barbershop.dsa.MyPriorityQueue;
import com.barbershop.model.Appointment;
import com.barbershop.service.QueueService;
import com.barbershop.util.Dialogs;
import com.barbershop.util.Money;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard: today's numbers, the next customer to serve and today's appointment list.
 *
 * <p>The "Next up" panel is DSA #2 in action — the day's pending appointments are pushed into a
 * hand-written {@link MyPriorityQueue} ordered by date and time, and the highest priority
 * appointment is peeked from the top of the heap.</p>
 */
public class DashboardController implements Refreshable {

    @FXML
    private Label appointmentsValue;

    @FXML
    private Label servedValue;

    @FXML
    private Label waitingValue;

    @FXML
    private Label revenueValue;

    @FXML
    private Label nextUpCustomer;

    @FXML
    private Label nextUpDetails;

    @FXML
    private Label nextUpNote;

    @FXML
    private TableView<Appointment> todayTable;

    @FXML
    private TableColumn<Appointment, String> timeColumn;

    @FXML
    private TableColumn<Appointment, String> customerColumn;

    @FXML
    private TableColumn<Appointment, String> barberColumn;

    @FXML
    private TableColumn<Appointment, String> serviceColumn;

    @FXML
    private TableColumn<Appointment, String> statusColumn;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final QueueService queueService = QueueService.getInstance();

    @FXML
    public void initialize() {
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTime"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        barberColumn.setCellValueFactory(new PropertyValueFactory<>("barberName"));
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new StatusCell());
    }

    @Override
    public void refresh() {
        try {
            LocalDate today = LocalDate.now();

            List<Appointment> todaysAppointments = appointmentDAO.findByDate(today);

            appointmentsValue.setText(String.valueOf(todaysAppointments.size()));
            servedValue.setText(String.valueOf(transactionDAO.countForDate(today)));
            waitingValue.setText(String.valueOf(queueService.size()));
            revenueValue.setText(Money.format(transactionDAO.totalRevenueForDate(today)));

            ObservableList<Appointment> rows = FXCollections.observableArrayList(todaysAppointments);
            todayTable.setItems(rows);
            todayTable.setPlaceholder(new Label("No appointments booked for today."));

            updateNextUp(todaysAppointments);
        } catch (DataAccessException e) {
            Dialogs.error("Dashboard", e.getMessage(), e);
        }
    }

    /**
     * Pushes every still-pending appointment into a custom priority queue and reads off the
     * one at the top of the heap.
     */
    private void updateNextUp(List<Appointment> todaysAppointments) {
        MyPriorityQueue<Appointment> pending = new MyPriorityQueue<>(Appointment.BY_DATE_TIME);

        for (Appointment appointment : todaysAppointments) {
            boolean stillPending = Appointment.BOOKED.equals(appointment.getStatus())
                    || Appointment.WAITING.equals(appointment.getStatus());
            if (stillPending) {
                pending.insert(appointment);
            }
        }

        if (pending.isEmpty()) {
            nextUpCustomer.setText("No pending appointments left today.");
            nextUpDetails.setText("");
            nextUpNote.setText("Priority queue empty — every booking has been served or cancelled.");
            return;
        }

        Appointment next = pending.peek();
        nextUpCustomer.setText(next.getFormattedTime() + "  " + next.getCustomerName());
        nextUpDetails.setText(next.getServiceName()
                + " with " + next.getBarberName()
                + "  •  " + Money.format(next.getServicePrice()));
        nextUpNote.setText("Priority queue (min-heap): " + pending.size()
                + " pending appointment(s), highest priority at the top.");
    }
}

