package com.barbershop.controller;

import com.barbershop.database.AppointmentDAO;
import com.barbershop.database.CustomerDAO;
import com.barbershop.database.DataAccessException;
import com.barbershop.dsa.MyLinkedList;
import com.barbershop.dsa.SearchAlgorithms;
import com.barbershop.dsa.SortAlgorithms;
import com.barbershop.model.Appointment;
import com.barbershop.model.Customer;
import com.barbershop.model.CustomerSummary;
import com.barbershop.util.Dialogs;
import com.barbershop.util.Money;
import com.barbershop.util.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CustomerController implements Refreshable {
    @FXML
    private TextField searchField;

    @FXML
    private Label searchInfoLabel;

    @FXML
    private TableView<CustomerSummary> customerTable;

    @FXML
    private TableColumn<CustomerSummary, String> nameColumn;

    @FXML
    private TableColumn<CustomerSummary, String> phoneColumn;

    @FXML
    private TableColumn<CustomerSummary, String> emailColumn;

    @FXML
    private TableColumn<CustomerSummary, Integer> countColumn;

    @FXML
    private TableColumn<CustomerSummary, String> spentColumn;

    @FXML
    private Label formTitle;

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private Label historyTitle;

    @FXML
    private Label historyNote;

    @FXML
    private ListView<String> historyList;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    private Customer selectedCustomer;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        countColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentCount"));
        spentColumn.setCellValueFactory(new PropertyValueFactory<>("formattedSpent"));

        customerTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, previous, selected) -> {
                    if (selected != null) {
                        loadCustomerIntoForm(selected.getCustomer());
                    }
                });

        historyList.setPlaceholder(new Label("No history to show yet."));
    }

    @Override
    public void refresh() {
        applySearch();
    }

    @FXML
    private void handleSearch() {
        applySearch();
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        applySearch();
    }

    private void applySearch() {
        try {
            String query = searchField.getText() == null ? "" : searchField.getText().trim();

            List<Customer> allCustomers = customerDAO.findAll();
            List<Customer> sortedByName =
                    SortAlgorithms.sortedCopy(allCustomers, Comparator.naturalOrder());

            List<Customer> matches;
            if (query.isEmpty()) {
                matches = sortedByName;
                searchInfoLabel.setText("Showing all " + sortedByName.size()
                        + " customers, ordered by name with merge sort.");
            } else {
                matches = searchCustomers(sortedByName, query);
            }

            showRows(matches);
        } catch (DataAccessException e) {
            Dialogs.error("Customers", e.getMessage(), e);
        }
    }

    private List<Customer> searchCustomers(List<Customer> sortedByName, String query) {
        String needle = query.toLowerCase();

        Customer probe = new Customer(query, "", "");
        int exactIndex = SearchAlgorithms.binarySearch(sortedByName, probe);

        List<Customer> matches = new ArrayList<>();
        for (Customer customer : sortedByName) {
            boolean nameContains = customer.getName() != null
                    && customer.getName().toLowerCase().contains(needle);
            boolean phoneContains = customer.getPhone() != null
                    && customer.getPhone().contains(query);
            if (nameContains || phoneContains) {
                matches.add(customer);
            }
        }

        if (exactIndex >= 0) {
            searchInfoLabel.setText("Binary search: exact match found at sorted position "
                    + exactIndex + " (O(log n)). " + matches.size() + " row(s) shown.");
        } else {
            searchInfoLabel.setText("Binary search: no exact name match. Linear scan (O(n)) found "
                    + matches.size() + " partial match(es).");
        }
        return matches;
    }

    private void showRows(List<Customer> customers) {
        List<CustomerSummary> rows = new ArrayList<>(customers.size());
        for (Customer customer : customers) {
            rows.add(new CustomerSummary(
                    customer,
                    customerDAO.countAppointments(customer.getCustomerId()),
                    customerDAO.totalSpent(customer.getCustomerId())));
        }

        ObservableList<CustomerSummary> items = FXCollections.observableArrayList(rows);
        customerTable.setItems(items);
        customerTable.setPlaceholder(new Label(
                customers.isEmpty() ? "No customers match your search." : "No customers yet."));
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();

        if (!validate(name, phone, email)) {
            return;
        }

        try {
            if (selectedCustomer == null) {
                Customer customer = new Customer(name, phone, Validator.normalizeEmail(email));
                customerDAO.insert(customer);
                Dialogs.info("Customer added", name + " has been registered.");
            } else {
                selectedCustomer.setName(name);
                selectedCustomer.setPhone(phone);
                selectedCustomer.setEmail(Validator.normalizeEmail(email));
                customerDAO.update(selectedCustomer);
                Dialogs.info("Customer updated", name + "'s details have been saved.");
            }
            handleClearForm();
            applySearch();
        } catch (DataAccessException e) {
            Dialogs.error("Customers", e.getMessage(), e);
        }
    }

    private boolean validate(String name, String phone, String email) {
        if (Validator.isBlank(name)) {
            Dialogs.warn("Missing name", "Please enter the customer's full name.");
            return false;
        }
        if (Validator.isBlank(phone)) {
            Dialogs.warn("Missing phone", "Please enter a contact number.");
            return false;
        }
        if (!Validator.isPhone(phone)) {
            Dialogs.warn("Invalid phone", "The phone number contains unexpected characters.");
            return false;
        }
        if (!Validator.isOptionalEmail(email)) {
            Dialogs.warn("Invalid email", "Please enter a valid email address or leave it empty.");
            return false;
        }

        int excludingId = selectedCustomer == null ? 0 : selectedCustomer.getCustomerId();
        if (customerDAO.phoneExists(phone, excludingId)) {
            Dialogs.warn("Duplicate phone", "Another customer already uses " + phone + ".");
            return false;
        }
        return true;
    }

    @FXML
    private void handleDelete() {
        if (selectedCustomer == null) {
            Dialogs.warn("No selection", "Select a customer in the table first.");
            return;
        }
        boolean confirmed = Dialogs.confirm("Delete customer",
                "Delete " + selectedCustomer.getName()
                        + "?\nTheir appointments and recorded payments will also be removed.");
        if (!confirmed) {
            return;
        }

        try {
            customerDAO.delete(selectedCustomer.getCustomerId());
            Dialogs.info("Customer deleted", "The customer has been removed.");
            handleClearForm();
            applySearch();
        } catch (DataAccessException e) {
            Dialogs.error("Customers", e.getMessage(), e);
        }
    }

    @FXML
    private void handleClearForm() {
        selectedCustomer = null;
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        formTitle.setText("NEW CUSTOMER");
        customerTable.getSelectionModel().clearSelection();
    }

    private void loadCustomerIntoForm(Customer customer) {
        selectedCustomer = customer;
        nameField.setText(customer.getName());
        phoneField.setText(customer.getPhone());
        emailField.setText(customer.getEmail());
        formTitle.setText("EDIT CUSTOMER");

        showHistory(customer);
    }

    @FXML
    private void handleViewHistory() {
        if (selectedCustomer == null) {
            Dialogs.warn("No selection", "Select a customer in the table first.");
            return;
        }
        showHistory(selectedCustomer);
    }

    private void showHistory(Customer customer) {
        try {
            List<Appointment> history = appointmentDAO.findByCustomer(customer.getCustomerId());

            MyLinkedList<Appointment> chain = new MyLinkedList<>();

            for (Appointment appointment : history) {
                chain.addLast(appointment);
            }

            List<String> lines = new ArrayList<>();
            int node = 1;
            for (Appointment appointment : chain) {
                lines.add(node++ + ".  " + appointment.getFormattedDate()
                        + "  " + appointment.getFormattedTime()
                        + "  •  " + appointment.getServiceName()
                        + " with " + appointment.getBarberName()
                        + "  •  " + Money.format(appointment.getServicePrice())
                        + "  [" + appointment.getStatus() + "]");
            }

            historyList.setItems(FXCollections.observableArrayList(lines));
            historyTitle.setText("SERVICE HISTORY — " + customer.getName());
            historyNote.setText(lines.isEmpty()
                    ? "This customer has no appointments yet."
                    : "Linked list with " + chain.size() + " node(s):  HEAD -> "
                            + "newest appointment -> ... -> null");
        } catch (DataAccessException e) {
            Dialogs.error("History", e.getMessage(), e);
        }
    }
}
