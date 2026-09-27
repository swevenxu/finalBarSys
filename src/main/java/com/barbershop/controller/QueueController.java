package com.barbershop.controller;

import com.barbershop.database.AppointmentDAO;
import com.barbershop.database.BarberDAO;
import com.barbershop.database.CustomerDAO;
import com.barbershop.database.DataAccessException;
import com.barbershop.database.ServiceDAO;
import com.barbershop.database.TransactionDAO;
import com.barbershop.model.Appointment;
import com.barbershop.model.Barber;
import com.barbershop.model.Customer;
import com.barbershop.model.QueueTicket;
import com.barbershop.model.Service;
import com.barbershop.model.Transaction;
import com.barbershop.service.QueueService;
import com.barbershop.util.Dialogs;
import com.barbershop.util.Money;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Walk-in queue screen.
 *
 * <p>This is where the project's own {@link QueueService} — built on the hand-written
 * {@code MyQueue} — drives the user interface. Adding a walk-in calls {@code enqueue()} at the
 * rear of the line; serving a customer calls {@code dequeue()} at the front, which records the
 * completed appointment and its payment.</p>
 */
public class QueueController implements Refreshable {

    @FXML
    private Label currentCustomer;

    @FXML
    private Label currentDetails;

    @FXML
    private Label currentTicket;

    @FXML
    private Button serveButton;

    @FXML
    private Label waitingHeading;

    @FXML
    private ListView<String> waitingList;

    @FXML
    private Label queueStructure;

    @FXML
    private ComboBox<Customer> customerCombo;

    @FXML
    private TextField newCustomerField;

    @FXML
    private ComboBox<Service> serviceCombo;

    @FXML
    private ComboBox<Barber> barberCombo;

    private final QueueService queueService = QueueService.getInstance();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final BarberDAO barberDAO = new BarberDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML
    public void initialize() {
        waitingList.setPlaceholder(new Label("Nobody is waiting. Add a walk-in below."));
    }

    @Override
    public void refresh() {
        try {
            customerCombo.setItems(FXCollections.observableArrayList(customerDAO.findAll()));
            serviceCombo.setItems(FXCollections.observableArrayList(serviceDAO.findAll()));
            barberCombo.setItems(FXCollections.observableArrayList(barberDAO.findAll()));
        } catch (DataAccessException e) {
            Dialogs.error("Queue", e.getMessage(), e);
        }
        refreshQueueView();
    }

    // ------------------------------------------------------------------
    // Queue display (DSA #1)
    // ------------------------------------------------------------------

    /** Redraws the front-of-line card, the waiting list and the queue structure diagram. */
    private void refreshQueueView() {
        QueueTicket next = queueService.peekNext();

        if (next == null) {
            currentCustomer.setText("Nobody is waiting.");
            currentDetails.setText("The queue is empty — the FRONT and REAR pointers are both null.");
            currentTicket.setText("");
        } else {
            currentCustomer.setText(next.getCustomerName());
            currentDetails.setText(next.getServiceName()
                    + "  •  " + Money.format(next.getPrice())
                    + "  •  barber " + next.getBarberName());
            currentTicket.setText("Ticket #" + next.getTicketId()
                    + "  •  enqueued at " + next.getEnqueuedAt().toLocalTime()
                            .withSecond(0).withNano(0));
        }

        serveButton.setDisable(next == null);

        List<String> rows = new ArrayList<>();
        int position = 1;
        for (QueueTicket ticket : queueService.snapshot()) {
            rows.add(position++ + ".  " + ticket.getCustomerName()
                    + "  —  " + ticket.getServiceName()
                    + "  (" + ticket.getBarberName() + ")");
        }
        waitingList.setItems(FXCollections.observableArrayList(rows));
        waitingHeading.setText("WAITING QUEUE (" + queueService.size() + ")");
        queueStructure.setText(buildQueueDiagram());
    }

    /** Builds the FRONT -> ... -> REAR string from the live queue contents. */
    private String buildQueueDiagram() {
        List<QueueTicket> tickets = queueService.snapshot();
        if (tickets.isEmpty()) {
            return "FRONT -> (empty) -> REAR";
        }
        StringBuilder diagram = new StringBuilder("FRONT -> ");
        for (int i = 0; i < tickets.size(); i++) {
            diagram.append(tickets.get(i).getCustomerName());
            diagram.append(i == tickets.size() - 1 ? "" : " -> ");
        }
        return diagram.append(" -> REAR").toString();
    }

    // ------------------------------------------------------------------
    // Queue operations
    // ------------------------------------------------------------------

    @FXML
    private void handleAddWalkIn() {
        try {
            Customer customer = resolveCustomer();
            if (customer == null) {
                return;
            }

            Service service = serviceCombo.getValue();
            if (service == null) {
                Dialogs.warn("Missing service", "Please choose the service the customer wants.");
                return;
            }

            Barber barber = barberCombo.getValue();
            if (barber == null) {
                Dialogs.warn("Missing barber", "Please choose the barber who will serve the customer.");
                return;
            }

            QueueTicket ticket = queueService.enqueue(customer, service, barber);
            Dialogs.info("Added to the queue",
                    customer.getName() + " joined the line as ticket #" + ticket.getTicketId()
                            + ".\nPosition: " + queueService.size() + " of "
                            + queueService.size() + ".");

            newCustomerField.clear();
            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Queue", e.getMessage(), e);
        }
    }

    /** Uses the selected customer, or registers the name typed in the optional text field. */
    private Customer resolveCustomer() {
        String newName = newCustomerField.getText() == null ? "" : newCustomerField.getText().trim();

        if (!newName.isEmpty()) {
            Customer customer = new Customer(newName, "", null);
            customerDAO.insert(customer);
            return customer;
        }

        Customer selected = customerCombo.getValue();
        if (selected == null) {
            Dialogs.warn("Missing customer",
                    "Choose a customer from the list, or type a new name in the box on the right.");
            return null;
        }
        return selected;
    }

    /**
     * Dequeues the customer at the front, records the completed appointment and takes the
     * payment — the "serve then pay" step of the system workflow.
     */
    @FXML
    private void handleServe() {
        QueueTicket ticket = queueService.peekNext();
        if (ticket == null) {
            Dialogs.warn("Queue empty", "There is nobody waiting to be served.");
            return;
        }

        boolean confirmed = Dialogs.confirm("Serve customer",
                "Serve " + ticket.getCustomerName() + "?\n"
                        + ticket.getServiceName() + " with " + ticket.getBarberName()
                        + "  •  " + Money.format(ticket.getPrice()));
        if (!confirmed) {
            return;
        }

        List<String> methods = List.of(Transaction.PAYMENT_METHODS);
        ChoiceDialog<String> paymentDialog = new ChoiceDialog<>(Transaction.CASH, methods);
        paymentDialog.setTitle("Payment");
        paymentDialog.setHeaderText(null);
        paymentDialog.setContentText(ticket.getCustomerName() + "  •  "
                + Money.format(ticket.getPrice()) + "\nPayment method:");
        Optional<String> chosenMethod = paymentDialog.showAndWait();
        if (chosenMethod.isEmpty()) {
            return;
        }

        try {
            // dequeue() removes the FRONT element and shifts the rest of the line forward.
            QueueTicket served = queueService.serveNext();

            Appointment appointment = new Appointment(
                    served.getCustomer().getCustomerId(),
                    served.getBarber().getBarberId(),
                    served.getService().getServiceId(),
                    LocalDate.now(),
                    LocalTime.now().withSecond(0).withNano(0),
                    Appointment.DONE);
            appointmentDAO.insert(appointment);

            Transaction transaction = new Transaction(
                    appointment.getAppointmentId(),
                    served.getPrice(),
                    chosenMethod.get());
            transactionDAO.insert(transaction);

            Dialogs.info("Customer served",
                    served.getCustomerName() + " has been served.\n"
                            + "Payment of " + Money.format(served.getPrice())
                            + " recorded as " + chosenMethod.get() + ".");

            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Queue", e.getMessage(), e);
        }
    }

    @FXML
    private void handleRemoveFront() {
        QueueTicket front = queueService.peekNext();
        if (front == null) {
            Dialogs.warn("Queue empty", "There is nobody waiting to be removed.");
            return;
        }
        if (!Dialogs.confirm("Remove customer",
                "Remove " + front.getCustomerName() + " from the front of the line?")) {
            return;
        }
        try {
            queueService.removeAt(0);
            refresh();
        } catch (RuntimeException e) {
            Dialogs.error("Queue", e.getMessage(), e);
        }
    }

    @FXML
    private void handleRemoveSelected() {
        int index = waitingList.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            Dialogs.warn("No selection", "Select a customer in the waiting list first.");
            return;
        }
        QueueTicket ticket = queueService.getAt(index);
        if (!Dialogs.confirm("Remove customer",
                "Remove " + ticket.getCustomerName() + " from the queue?")) {
            return;
        }
        try {
            queueService.removeAt(index);
            refresh();
        } catch (RuntimeException e) {
            Dialogs.error("Queue", e.getMessage(), e);
        }
    }
}
