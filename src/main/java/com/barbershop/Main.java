package com.barbershop;

import com.barbershop.controller.LoginController;
import com.barbershop.database.DatabaseInitializer;
import com.barbershop.util.Dialogs;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX application entry point.
 *
 * <p>On startup it prepares the SQLite database (creating the schema and seeding demo data the
 * first time) and then shows the login screen.</p>
 */
public class Main extends Application {

    public static final String APP_NAME = "Barbershop Management System";

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseInitializer.initialize();
        } catch (RuntimeException e) {
            Dialogs.error("Database error",
                    "The database could not be prepared:\n" + e.getMessage(), e);
            return;
        }

        LoginController.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
