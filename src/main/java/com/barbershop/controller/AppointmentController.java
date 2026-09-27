package com.barbershop.controller;

import com.barbershop.database.AppointmentDAO;
import com.barbershop.database.BarberDAO;
import com.barbershop.database.CustomerDAO;
import com.barbershop.database.DataAccessException;
import com.barbershop.database.ServiceDAO;
import com.barbershop.dsa.SortAlgorithms;
import com.barbershop.model.Appointment;
import com.barbershop.model.Barber;
import com.barbershop.model.Customer;
import com.barbershop.model.Service;
import com.barbershop.util.Dialogs;
import com.barbershop.util.Money;
import com.barbershop.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Appointment booking and management.
 *
 * <p>Sorting is the visible DSA here (DSA #3): the table is ordered with the project's own
 * {@link SortAlgorithms#mergeSort} rather than the TableView's built-in column sort, so the
 * sort can be explained and timed as an algorithmic step. The sorts available match the project
 * plan: appointment time, customer name, barber, service and price.</p>
 */
public class AppointmentController implements Refreshable {

    private static final String ASCENDING = "Ascending";
    private static final String DESCENDING = "Descending";

    /** The sortable keys shown in the "Sort by" combo box. */
    private enum SortField {
        TIME("Appointment time",
                Comparator.comparing(Appointment::getDateTime,
                        Comparator.nullsLast(Comparator.naturalOrder()))),
        CUSTOMER("Customer name",
                Comparator.comparing(Appointment::getCustomerName, String.CASE_INSENSITIVE_ORDER)),
        BARBER("Barber",
                Comparator.comparing(Appointment::getBarberName, String.CASE_INSENSITIVE_ORDER)),
        SERVICE("Service",
                Comparator.comparing(Appointment::getServiceName, String.CASE_INSENSITIVE_ORDER)),
        PRICE("Price",
                Comparator.comparingDouble(Appointment::getServicePrice));

        private final String label;
        private final Comparator<Appointment> comparator;

        SortField(String label, Comparator<Appointment> comparator) {
            this.label = label;
            this.comparator = comparator;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @FXML
    private ComboBox<SortField> sortByCombo;

    @FXML
    private ComboBox<String> sortOrderCombo;

    @FXML
    private TextField filterField;

    @FXML
    private Label sortInfoLabel;

    @FXML
    private TableView<Appointment> appointmentTable;

    @FXML
    private TableColumn<Appointment, String> dateColumn;

    @FXML
    private TableColumn<Appointment, String> timeColumn;

    @FXML
    private TableColumn<Appointment, String> customerColumn;

    @FXML
    private TableColumn<Appointment, String> barberColumn;

    @FXML
    private TableColumn<Appointment, String> serviceColumn;

    @FXML
    private TableColumn<Appointment, Double> priceColumn;

    @FXML
    private TableColumn<Appointment, String> statusColumn;

    @FXML
    private Label formTitle;

    @FXML
    private ComboBox<Customer> customerCombo;

    @FXML
    private ComboBox<Barber> barberCombo;

    @FXML
    private ComboBox<Service> serviceCombo;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField timeField;

    @FXML
    private ComboBox<String> statusCombo;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BarberDAO barberDAO = new BarberDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();

    /** Every appointment loaded from the database. */
    private List<Appointment> allAppointments = new ArrayList<>();

    /** The currently selected appointment, or {@code null} when booking a new one. */
    private Appointment selectedAppointment;

    @FXML
    public void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTime"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        barberColumn.setCellValueFactory(new PropertyValueFactory<>("barberName"));
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("servicePrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : Money.format(price));
            }
        });
        statusColumn.setCellFactory(column -> new StatusCell());

        sortByCombo.setItems(FXCollections.observableArrayList(SortField.values()));
        sortByCombo.getSelectionModel().select(SortField.TIME);
        sortOrderCombo.setItems(FXCollections.observableArrayList(ASCENDING, DESCENDING));
        sortOrderCombo.getSelectionModel().select(ASCENDING);

        statusCombo.setItems(FXCollections.observableArrayList(Appointment.STATUSES));
        statusCombo.getSelectionModel().select(Appointment.BOOKED);

        datePicker.setValue(LocalDate.now());

        appointmentTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, previous, selected) -> {
                    if (selected != null) {
                        loadIntoForm(selected);
                    }
                });

        appointmentTable.setPlaceholder(new Label("No appointments to show."));
    }

    @Override
    public void refresh() {
        try {
            customerCombo.setItems(FXCollections.observableArrayList(customerDAO.findAll()));
            barberCombo.setItems(FXCollections.observableArrayList(barberDAO.findAll()));
            serviceCombo.setItems(FXCollections.observableArrayList(serviceDAO.findAll()));

            allAppointments = appointmentDAO.findAllWithDetails();
        } catch (DataAccessException e) {
            Dialogs.error("Appointments", e.getMessage(), e);
            return;
        }
        renderTable();
    }

    // ------------------------------------------------------------------
    // Sorting and filtering (DSA #3)
    // ------------------------------------------------------------------

    @FXML
    private void handleSort() {
        renderTable();
    }

    @FXML
    private void handleFilter() {
        renderTable();
    }

    @FXML
    private void handleClearFilter() {
        filterField.clear();
        renderTable();
    }

    /** Applies the text filter, then sorts what is left with the custom merge sort. */
    private void renderTable() {
        String filter = filterField.getText() == null ? "" : filterField.getText().trim().toLowerCase();

        List<Appointment> rows = new ArrayList<>();
        for (Appointment appointment : allAppointments) {
            if (filter.isEmpty() || matchesFilter(appointment, filter)) {
                rows.add(appointment);
            }
        }

        SortField field = sortByCombo.getValue() == null ? SortField.TIME : sortByCombo.getValue();
        boolean ascending = !DESCENDING.equals(sortOrderCombo.getValue());
        Comparator<Appointment> comparator = ascending ? field.comparator : field.comparator.reversed();

        SortAlgorithms.mergeSort(rows, comparator);

        appointmentTable.setItems(FXCollections.observableArrayList(rows));

        String order = ascending ? "ascending" : "descending";
        String filterNote = filter.isEmpty()
                ? ""
                : "  •  filtered by \"" + filterField.getText().trim() + "\"";
        sortInfoLabel.setText("Merge sort (O(n log n), stable) by " + field.label + ", " + order
                + "  •  " + rows.size() + " of " + allAppointments.size()
                + " appointment(s) shown" + filterNote + ".");
    }

    private boolean matchesFilter(Appointment appointment, String filter) {
        return appointment.getCustomerName().toLowerCase().contains(filter)
                || appointment.getBarberName().toLowerCase().contains(filter)
                || appointment.getServiceName().toLowerCase().contains(filter)
                || appointment.getStatus().toLowerCase().contains(filter);
    }

    // ------------------------------------------------------------------
    // Booking CRUD
    // ------------------------------------------------------------------

    @FXML
    private void handleSave() {
        Customer customer = customerCombo.getValue();
        Barber barber = barberCombo.getValue();
        Service service = serviceCombo.getValue();
        LocalDate date = datePicker.getValue();

        if (customer == null || barber == null || service == null) {
            Dialogs.warn("Incomplete booking",
                    "Please choose the customer, the barber and the service.");
            return;
        }
        if (date == null) {
            Dialogs.warn("Missing date", "Please choose the appointment date.");
            return;
        }

        LocalTime time;
        try {
            time = Validator.parseTime(timeField.getText());
        } catch (IllegalArgumentException e) {
            Dialogs.warn("Invalid time", e.getMessage());
            return;
        }

        String status = statusCombo.getValue() == null ? Appointment.BOOKED : statusCombo.getValue();

        int editingId = selectedAppointment == null ? 0 : selectedAppointment.getAppointmentId();
        Appointment clash = findClash(barber.getBarberId(), date, time, editingId);
        if (clash != null && !Appointment.CANCELLED.equals(status)) {
            Dialogs.warn("Time slot taken",
                    barber.getName() + " already has an appointment at "
                            + time + " on " + date + " for " + clash.getCustomerName()
                            + ".\nChoose another time or another barber.");
            return;
        }

        try {
            if (selectedAppointment == null) {
                Appointment appointment =
                        new Appointment(customer.getCustomerId(), barber.getBarberId(),
                                service.getServiceId(), date, time, status);
                appointmentDAO.insert(appointment);
                Dialogs.info("Appointment booked",
                        customer.getName() + " is booked with " + barber.getName()
                                + " on " + date + " at " + time + ".");
            } else {
                selectedAppointment.setCustomerId(customer.getCustomerId());
                selectedAppointment.setBarberId(barber.getBarberId());
                selectedAppointment.setServiceId(service.getServiceId());
                selectedAppointment.setAppointmentDate(date);
                selectedAppointment.setAppointmentTime(time);
                selectedAppointment.setStatus(status);
                appointmentDAO.update(selectedAppointment);
                Dialogs.info("Appointment updated", "The appointment has been saved.");
            }
            handleClearForm();
            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Appointments", e.getMessage(), e);
        }
    }

    /** Looks for an existing appointment for the same barber at the same date and time. */
    private Appointment findClash(int barberId, LocalDate date, LocalTime time, int excludingId) {
        for (Appointment appointment : allAppointments) {
            if (appointment.getAppointmentId() == excludingId) {
                continue;
            }
            if (appointment.getBarberId() == barberId
                    && date.equals(appointment.getAppointmentDate())
                    && time.equals(appointment.getAppointmentTime())
                    && !Appointment.CANCELLED.equals(appointment.getStatus())) {
                return appointment;
            }
        }
        return null;
    }

    @FXML
    private void handleCancelAppointment() {
        if (selectedAppointment == null) {
            Dialogs.warn("No selection", "Select an appointment in the table first.");
            return;
        }
        if (!Dialogs.confirm("Cancel appointment",
                "Mark the appointment for " + selectedAppointment.getCustomerName()
                        + " as cancelled?")) {
            return;
        }
        try {
            appointmentDAO.updateStatus(selectedAppointment.getAppointmentId(), Appointment.CANCELLED);
            Dialogs.info("Appointment cancelled", "The appointment has been cancelled.");
            handleClearForm();
            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Appointments", e.getMessage(), e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedAppointment == null) {
            Dialogs.warn("No selection", "Select an appointment in the table first.");
            return;
        }
        if (!Dialogs.confirm("Delete appointment",
                "Permanently delete the appointment for "
                        + selectedAppointment.getCustomerName()
                        + "?\nAny recorded payment for it will also be removed.")) {
            return;
        }
        try {
            appointmentDAO.delete(selectedAppointment.getAppointmentId());
            Dialogs.info("Appointment deleted", "The appointment has been removed.");
            handleClearForm();
            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Appointments", e.getMessage(), e);
        }
    }

    @FXML
    private void handleClearForm() {
        selectedAppointment = null;
        customerCombo.getSelectionModel().clearSelection();
        barberCombo.getSelectionModel().clearSelection();
        serviceCombo.getSelectionModel().clearSelection();
        datePicker.setValue(LocalDate.now());
        timeField.clear();
        statusCombo.getSelectionModel().select(Appointment.BOOKED);
        formTitle.setText("BOOK APPOINTMENT");
        appointmentTable.getSelectionModel().clearSelection();
    }

    private void loadIntoForm(Appointment appointment) {
        selectedAppointment = appointment;

        selectById(customerCombo, appointment.getCustomerId(), Customer::getCustomerId);
        selectById(barberCombo, appointment.getBarberId(), Barber::getBarberId);
        selectById(serviceCombo, appointment.getServiceId(), Service::getServiceId);

        datePicker.setValue(appointment.getAppointmentDate());
        timeField.setText(appointment.getFormattedTime());
        statusCombo.getSelectionModel().select(appointment.getStatus());
        formTitle.setText("EDIT APPOINTMENT");
    }

    /** Selects the combo box entry whose id matches, using the supplied id accessor. */
    private <T> void selectById(ComboBox<T> combo, int id, ToIntFunction<T> idAccessor) {
        for (T item : combo.getItems()) {
            if (idAccessor.applyAsInt(item) == id) {
                combo.getSelectionModel().select(item);
                return;
            }
        }
    }
}
