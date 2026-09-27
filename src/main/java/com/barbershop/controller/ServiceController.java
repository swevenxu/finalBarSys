package com.barbershop.controller;

import com.barbershop.database.DataAccessException;
import com.barbershop.database.ServiceDAO;
import com.barbershop.model.Service;
import com.barbershop.util.Dialogs;
import com.barbershop.util.Money;
import com.barbershop.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ServiceController implements Refreshable {
    @FXML
    private TableView<Service> serviceTable;

    @FXML
    private TableColumn<Service, String> nameColumn;

    @FXML
    private TableColumn<Service, Double> priceColumn;

    @FXML
    private TableColumn<Service, Integer> durationColumn;

    @FXML
    private Label formTitle;

    @FXML
    private TextField nameField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField durationField;

    private final ServiceDAO serviceDAO = new ServiceDAO();

    private Service selectedService;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));

        priceColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : Money.format(price));
            }
        });

        serviceTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, previous, selected) -> {
                    if (selected != null) {
                        loadIntoForm(selected);
                    }
                });

        serviceTable.setPlaceholder(new Label("No services yet."));
    }

    @Override
    public void refresh() {
        try {
            serviceTable.setItems(FXCollections.observableArrayList(serviceDAO.findAll()));
        } catch (DataAccessException e) {
            Dialogs.error("Services", e.getMessage(), e);
        }
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String priceText = priceField.getText() == null ? "" : priceField.getText().trim();
        String durationText = durationField.getText() == null ? "" : durationField.getText().trim();

        if (Validator.isBlank(name)) {
            Dialogs.warn("Missing name", "Please enter the service name.");
            return;
        }

        double price;
        int duration;
        try {
            price = Money.parse(priceText);
            if (price < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Dialogs.warn("Invalid price", "Enter the price as a number, for example 150 or 250.50.");
            return;
        }

        try {
            duration = Integer.parseInt(durationText);
            if (duration <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Dialogs.warn("Invalid duration", "Enter the duration in whole minutes, for example 30.");
            return;
        }

        try {
            if (selectedService == null) {
                serviceDAO.insert(new Service(name, price, duration));
                Dialogs.info("Service added", name + " is now available.");
            } else {
                selectedService.setName(name);
                selectedService.setPrice(price);
                selectedService.setDurationMinutes(duration);
                serviceDAO.update(selectedService);
                Dialogs.info("Service updated", name + " has been saved.");
            }
            handleClearForm();
            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Services", e.getMessage(), e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedService == null) {
            Dialogs.warn("No selection", "Select a service in the table first.");
            return;
        }
        if (!Dialogs.confirm("Delete service",
                "Delete " + selectedService.getName() + "?")) {
            return;
        }
        try {
            serviceDAO.delete(selectedService.getServiceId());
            Dialogs.info("Service deleted", "The service has been removed.");
            handleClearForm();
            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Cannot delete service", e.getMessage(), e);
        }
    }

    @FXML
    private void handleClearForm() {
        selectedService = null;
        nameField.clear();
        priceField.clear();
        durationField.clear();
        formTitle.setText("NEW SERVICE");
        serviceTable.getSelectionModel().clearSelection();
    }

    private void loadIntoForm(Service service) {
        selectedService = service;
        nameField.setText(service.getName());
        priceField.setText(String.valueOf(service.getPrice()));
        durationField.setText(String.valueOf(service.getDurationMinutes()));
        formTitle.setText("EDIT SERVICE");
    }
}
