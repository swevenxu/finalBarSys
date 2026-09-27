package com.barbershop.controller;

import com.barbershop.database.BarberDAO;
import com.barbershop.database.DataAccessException;
import com.barbershop.model.Barber;
import com.barbershop.util.Dialogs;
import com.barbershop.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Barber management.
 */
public class BarberController implements Refreshable {

    @FXML
    private TableView<Barber> barberTable;

    @FXML
    private TableColumn<Barber, String> nameColumn;

    @FXML
    private TableColumn<Barber, String> specializationColumn;

    @FXML
    private TableColumn<Barber, String> statusColumn;

    @FXML
    private Label formTitle;

    @FXML
    private TextField nameField;

    @FXML
    private TextField specializationField;

    @FXML
    private ComboBox<String> statusCombo;

    private final BarberDAO barberDAO = new BarberDAO();

    private Barber selectedBarber;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusCombo.setItems(FXCollections.observableArrayList(Barber.STATUSES));
        statusCombo.getSelectionModel().select(Barber.AVAILABLE);

        barberTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, previous, selected) -> {
                    if (selected != null) {
                        loadIntoForm(selected);
                    }
                });

        barberTable.setPlaceholder(new Label("No barbers yet."));
    }

    @Override
    public void refresh() {
        try {
            barberTable.setItems(FXCollections.observableArrayList(barberDAO.findAll()));
        } catch (DataAccessException e) {
            Dialogs.error("Barbers", e.getMessage(), e);
        }
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String specialization = specializationField.getText() == null
                ? "" : specializationField.getText().trim();
        String status = statusCombo.getValue();

        if (Validator.isBlank(name)) {
            Dialogs.warn("Missing name", "Please enter the barber's full name.");
            return;
        }

        try {
            if (selectedBarber == null) {
                Barber barber = new Barber(name, specialization, status);
                barberDAO.insert(barber);
                Dialogs.info("Barber added", name + " has been added to the team.");
            } else {
                selectedBarber.setName(name);
                selectedBarber.setSpecialization(specialization);
                selectedBarber.setStatus(status);
                barberDAO.update(selectedBarber);
                Dialogs.info("Barber updated", name + "'s details have been saved.");
            }
            handleClearForm();
            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Barbers", e.getMessage(), e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedBarber == null) {
            Dialogs.warn("No selection", "Select a barber in the table first.");
            return;
        }
        if (!Dialogs.confirm("Delete barber",
                "Delete " + selectedBarber.getName() + "?")) {
            return;
        }
        try {
            barberDAO.delete(selectedBarber.getBarberId());
            Dialogs.info("Barber deleted", "The barber has been removed.");
            handleClearForm();
            refresh();
        } catch (DataAccessException e) {
            Dialogs.error("Cannot delete barber", e.getMessage(), e);
        }
    }

    @FXML
    private void handleClearForm() {
        selectedBarber = null;
        nameField.clear();
        specializationField.clear();
        statusCombo.getSelectionModel().select(Barber.AVAILABLE);
        formTitle.setText("NEW BARBER");
        barberTable.getSelectionModel().clearSelection();
    }

    private void loadIntoForm(Barber barber) {
        selectedBarber = barber;
        nameField.setText(barber.getName());
        specializationField.setText(barber.getSpecialization());
        statusCombo.getSelectionModel().select(barber.getStatus());
        formTitle.setText("EDIT BARBER");
    }
}
