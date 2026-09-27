package com.barbershop.controller;

import com.barbershop.database.AppointmentDAO;
import com.barbershop.database.DataAccessException;
import com.barbershop.database.TransactionDAO;
import com.barbershop.dsa.MyStack;
import com.barbershop.model.Appointment;
import com.barbershop.model.Transaction;
import com.barbershop.util.Dialogs;
import com.barbershop.util.Money;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Payment history and the recently-completed-transactions stack.
 *
 * <p>DSA #6 in the running system: every payment is pushed onto the project's own
 * {@link MyStack}, so the newest payment is always on top and "Undo last payment" is a
 * {@code peek()} followed by a {@code pop()}.</p>
 */
public class TransactionController implements Refreshable {

    @FXML
    private Label totalRevenueValue;

    @FXML
    private Label todayRevenueValue;

    @FXML
    private Label countValue;

    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private TableColumn<Transaction, String> dateColumn;

    @FXML
    private TableColumn<Transaction, String> customerColumn;

    @FXML
    private TableColumn<Transaction, String> serviceColumn;

    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private TableColumn<Transaction, String> methodColumn;

    @FXML
    private ListView<String> recentList;

    @FXML
    private Label stackNote;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    /** Completed payments held in LIFO order; the newest payment is on top. */
    private final MyStack<Transaction> recentPayments = new MyStack<>();

    @FXML
    public void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : Money.format(amount));
            }
        });

        transactionTable.setPlaceholder(new Label("No payments recorded yet."));
        recentList.setPlaceholder(new Label("The stack is empty."));
    }

    @Override
    public void refresh() {
        try {
            List<Transaction> transactions = transactionDAO.findAllWithDetails();

            transactionTable.setItems(FXCollections.observableArrayList(transactions));

            totalRevenueValue.setText(Money.format(transactionDAO.totalRevenue()));
            todayRevenueValue.setText(Money.format(transactionDAO.totalRevenueForDate(LocalDate.now())));
            countValue.setText(String.valueOf(transactions.size()));

            rebuildStack(transactions);
        } catch (DataAccessException e) {
            Dialogs.error("Transactions", e.getMessage(), e);
        }
    }

    /**
     * Pushes the payments onto the stack oldest first, so the newest payment ends up on top of
     * the stack where the UI shows it.
     */
    private void rebuildStack(List<Transaction> newestFirst) {
        recentPayments.clear();
        for (int i = newestFirst.size() - 1; i >= 0; i--) {
            recentPayments.push(newestFirst.get(i));
        }

        List<String> rows = new ArrayList<>();
        int position = 1;
        // toList() returns the stack top first, which is the order we display it in.
        for (Transaction transaction : recentPayments.toList()) {
            rows.add(position++ + ".  " + Money.format(transaction.getAmount())
                    + "  •  " + transaction.getCustomerName()
                    + "  •  " + transaction.getFormattedDate());
        }
        recentList.setItems(FXCollections.observableArrayList(rows));

        Transaction top = recentPayments.peekOrNull();
        stackNote.setText(top == null
                ? "Stack is empty — push happens when a payment is recorded."
                : "Stack of " + recentPayments.size() + " payment(s). TOP = #"
                        + top.getTransactionId() + " "
                        + Money.format(top.getAmount()) + " (" + top.getCustomerName() + ").");
    }

    @FXML
    private void handleRefresh() {
        refresh();
    }

    /** Pops the most recent payment off the stack and deletes it. */
    @FXML
    private void handleUndo() {
        Transaction top = recentPayments.peekOrNull();
        if (top == null) {
            Dialogs.warn("Nothing to undo", "No payments have been recorded yet.");
            return;
        }

        boolean confirmed = Dialogs.confirm("Undo last payment",
                "Undo the most recent payment?\n\n"
                        + top.getCustomerName() + "  •  " + Money.format(top.getAmount())
                        + "  •  " + top.getFormattedDate()
                        + "\n\nThe appointment itself stays marked as done.");
        if (!confirmed) {
            return;
        }

        try {
            transactionDAO.delete(top.getTransactionId());
            Dialogs.info("Payment undone",
                    "The payment of " + Money.format(top.getAmount())
                            + " for " + top.getCustomerName() + " has been removed.");
            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Transactions", e.getMessage(), e);
        }
    }

    /**
     * Records a payment for a completed appointment that has none yet — for example a booking
     * that was served from the Appointments screen rather than through the walk-in queue.
     */
    @FXML
    private void handleRecordPayment() {
        try {
            List<Appointment> unpaid = new ArrayList<>();
            for (Appointment appointment : appointmentDAO.findAllWithDetails()) {
                if (Appointment.DONE.equals(appointment.getStatus())
                        && transactionDAO.findByAppointment(appointment.getAppointmentId()).isEmpty()) {
                    unpaid.add(appointment);
                }
            }

            if (unpaid.isEmpty()) {
                Dialogs.info("Nothing to charge",
                        "Every completed appointment already has a recorded payment.");
                return;
            }

            ChoiceDialog<Appointment> appointmentDialog =
                    new ChoiceDialog<>(unpaid.get(0), unpaid);
            appointmentDialog.setTitle("Record payment");
            appointmentDialog.setHeaderText(null);
            appointmentDialog.setContentText("Completed appointment:");
            Optional<Appointment> chosenAppointment = appointmentDialog.showAndWait();
            if (chosenAppointment.isEmpty()) {
                return;
            }

            Appointment appointment = chosenAppointment.get();

            ChoiceDialog<String> methodDialog = new ChoiceDialog<>(Transaction.CASH,
                    List.of(Transaction.PAYMENT_METHODS));
            methodDialog.setTitle("Record payment");
            methodDialog.setHeaderText(null);
            methodDialog.setContentText(Money.format(appointment.getServicePrice())
                    + " for " + appointment.getCustomerName() + "\nPayment method:");
            Optional<String> chosenMethod = methodDialog.showAndWait();
            if (chosenMethod.isEmpty()) {
                return;
            }

            Transaction transaction = new Transaction(
                    appointment.getAppointmentId(),
                    appointment.getServicePrice(),
                    chosenMethod.get());
            transactionDAO.insert(transaction);

            Dialogs.info("Payment recorded",
                    Money.format(transaction.getAmount()) + " recorded for "
                            + appointment.getCustomerName() + ".");
            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Transactions", e.getMessage(), e);
        }
    }
}
