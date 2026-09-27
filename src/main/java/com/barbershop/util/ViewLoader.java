package com.barbershop.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

public final class ViewLoader {
    private static final String FXML_DIR = "/fxml/";

    private ViewLoader() { }

    public static Parent load(String fileName) {
        return loadWithController(fileName).root();
    }

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

    public record LoadedView(Parent root, Object controller) {
    }
}
