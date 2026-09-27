package com.barbershop.controller;

import com.barbershop.util.Dialogs;
import com.barbershop.util.Session;
import com.barbershop.util.Stylesheets;
import com.barbershop.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * The application shell: header, sidebar navigation and the content area.
 *
 * <p>Each navigation button loads a screen's FXML into the content area and then asks its
 * controller to refresh, so switching screens always shows up-to-date data.</p>
 */
public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label userLabel;

    @FXML
    private Label pageTitle;

    /** Opens the main window in {@code stage}. */
    public static void show(Stage stage) {
        Parent root = ViewLoader.load("MainView.fxml");
        Scene scene = new Scene(root, 1180, 720);
        Stylesheets.apply(scene);

        stage.setTitle("Barbershop Management System");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    public void initialize() {
        String role = Session.getCurrentUser() == null
                ? ""
                : " (" + Session.getCurrentUser().getRole() + ")";
        userLabel.setText(Session.getDisplayName() + role);
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        setContent("DashboardView.fxml", "Dashboard");
    }

    @FXML
    private void showAppointments() {
        setContent("AppointmentsView.fxml", "Appointments");
    }

    @FXML
    private void showCustomers() {
        setContent("CustomersView.fxml", "Customers");
    }

    @FXML
    private void showBarbers() {
        setContent("BarbersView.fxml", "Barbers");
    }

    @FXML
    private void showServices() {
        setContent("ServicesView.fxml", "Services");
    }

    @FXML
    private void showQueue() {
        setContent("QueueView.fxml", "Walk-in Queue");
    }

    @FXML
    private void showTransactions() {
        setContent("TransactionsView.fxml", "Transactions");
    }

    @FXML
    private void handleLogout() {
        if (!Dialogs.confirm("Sign out", "Sign out of the system?")) {
            return;
        }
        Session.logout();
        LoginController.show((Stage) contentArea.getScene().getWindow());
    }

    /**
     * Replaces the content area with a freshly loaded view and refreshes it.
     *
     * <p>Views are reloaded rather than cached so that every visit reflects the latest database
     * contents.</p>
     */
    private void setContent(String fxmlFile, String title) {
        try {
            ViewLoader.LoadedView view = ViewLoader.loadWithController(fxmlFile);
            contentArea.getChildren().setAll(view.root());
            pageTitle.setText(title);

            if (view.controller() instanceof Refreshable refreshable) {
                refreshable.refresh();
            }
        } catch (RuntimeException e) {
            Dialogs.error("Could not open screen",
                    "The " + title + " screen could not be opened.\n" + e.getMessage(), e);
        }
    }
}
