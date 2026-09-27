package com.barbershop;

import com.barbershop.database.DatabaseConnection;
import com.barbershop.database.DatabaseInitializer;
import com.barbershop.model.User;
import com.barbershop.util.Session;
import com.barbershop.util.Stylesheets;
import com.barbershop.util.ViewLoader;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FxmlSmokeTest {
    private static final List<String> VIEWS = List.of(
            "LoginView.fxml",
            "MainView.fxml",
            "DashboardView.fxml",
            "AppointmentsView.fxml",
            "CustomersView.fxml",
            "BarbersView.fxml",
            "ServicesView.fxml",
            "QueueView.fxml",
            "TransactionsView.fxml");

    @BeforeAll
    static void prepare() throws Exception {
        DatabaseConnection.setUrl("jdbc:sqlite:target/test-fxml.db");
        DatabaseInitializer.reset();

        User user = new User("admin", "hash", "Shop Administrator", User.ROLE_ADMIN);
        Session.login(user);

        startJavaFxToolkit();
    }

    private static void startJavaFxToolkit() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyRunning) {
            started.countDown();
        }
        assertTrue(started.await(30, TimeUnit.SECONDS), "the JavaFX toolkit did not start");
        Platform.setImplicitExit(false);
    }

    @Test
    @DisplayName("every FXML view loads with its controller bound")
    void allViewsLoad() {
        for (String view : VIEWS) {
            runOnFxThread(() -> {
                ViewLoader.LoadedView loaded = ViewLoader.loadWithController(view);
                assertNotNull(loaded.root(), view + " produced no scene graph root");
                assertNotNull(loaded.controller(), view + " produced no controller");
            });
        }
    }

    @Test
    @DisplayName("the shared stylesheet is on the classpath")
    void stylesheetIsPresent() {
        assertNotNull(Stylesheets.class.getResource(Stylesheets.STYLESHEET),
                "style.css should be packaged in src/main/resources/css");
    }

    private static void runOnFxThread(Runnable action) {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });

        try {
            assertTrue(done.await(30, TimeUnit.SECONDS), "the JavaFX task did not finish in time");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for the JavaFX thread");
        }

        if (failure.get() != null) {
            throw new AssertionError("Loading a view failed on the FX thread", failure.get());
        }
    }
}
