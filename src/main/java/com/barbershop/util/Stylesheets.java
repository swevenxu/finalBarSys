package com.barbershop.util;

import javafx.scene.Scene;

import java.net.URL;

/**
 * Attaches the shared JavaFX stylesheet to a scene.
 */
public final class Stylesheets {

    public static final String STYLESHEET = "/css/style.css";

    private Stylesheets() {
        // utility class
    }

    public static void apply(Scene scene) {
        URL stylesheet = Stylesheets.class.getResource(STYLESHEET);
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }
}
