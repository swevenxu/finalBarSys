package com.barbershop.controller;

import com.barbershop.database.DataAccessException;
import com.barbershop.database.UserDAO;
import com.barbershop.model.User;
import com.barbershop.util.Dialogs;
import com.barbershop.util.PasswordUtil;
import com.barbershop.util.Session;
import com.barbershop.util.Stylesheets;
import com.barbershop.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginController {
    private static final String TITLE = "Barbershop Management System";

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    public static void show(Stage stage) {
        Parent root = ViewLoader.load("LoginView.fxml");
        Scene scene = new Scene(root, 440, 580);
        Stylesheets.apply(scene);

        stage.setTitle(TITLE + " — Sign in");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both your username and password.");
            return;
        }

        try {
            Optional<User> authenticated =
                    userDAO.authenticate(username, PasswordUtil.hash(password));

            if (authenticated.isEmpty()) {
                messageLabel.setText("Invalid username or password. Please try again.");
                passwordField.clear();
                passwordField.requestFocus();
                return;
            }

            Session.login(authenticated.get());
            messageLabel.setText("");
            MainController.show((Stage) usernameField.getScene().getWindow());
        } catch (DataAccessException e) {
            Dialogs.error("Sign in failed", e.getMessage(), e);
        }
    }
}
