package com.barbershop.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

/**
 * Loads FXML screens.
 *
 * <p>All views live in {@code src/main/resources/fxml}. {@link #loadWithController(String)}
 * also hands back the screen's controller so the navigation code can call {@code refresh()}
 * on it.</p>
 */
public final class ViewLoader {

    private static final String FXML_DIR = "/fxml/";

    private ViewLoader() {
        // utility class
    }

    /** Loads a view by file name, for example {@code "DashboardView.fxml"}. */
    public static Parent load(String fileName) {
        return loadWithController(fileName).root();
    }

    /**
     * Loads a view together with its controller.
     *
     * @throws IllegalStateException when the FXML file is missing or cannot be parsed
     */
    public static LoadedView loadWithController(String fileName) {
        URL resource = ViewLoader.class.getResource(FXML_DIR + fileName);
        if (resource == null) {
            throw new IllegalStateException("FXML view not found on the classpath: " + FXML_DIR + fileName);
        }
        FXMLLoader loader = new FXMLLoader(resource);
        try {
            Parent root = loader.load();
            return new LoadedView(root, loader.getController());
        } catch (IOException e) {
            throw new IllegalStateException("Could not load the view " + fileName + ": " + e.getMessage(), e);
        }
    }

    /** A loaded view: the scene graph root plus the controller instance FXML created for it. */
    public record LoadedView(Parent root, Object controller) {
    }
}
