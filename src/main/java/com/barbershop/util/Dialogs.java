package com.barbershop.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

import java.util.Optional;

public final class Dialogs {
    private Dialogs() { }

    public static void info(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message, null);
    }

    public static void warn(String title, String message) {
        show(Alert.AlertType.WARNING, title, message, null);
    }

    public static void error(String title, String message) {
        show(Alert.AlertType.ERROR, title, message, null);
    }

    public static void error(String title, String message, Throwable cause) {
        show(Alert.AlertType.ERROR, title, message, cause);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private static void show(Alert.AlertType type, String title, String message, Throwable cause) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (cause != null) {
            TextArea details = new TextArea(cause.toString());
            details.setEditable(false);
            details.setWrapText(true);
            details.setMaxWidth(Double.MAX_VALUE);
            details.setMaxHeight(Double.MAX_VALUE);
            alert.getDialogPane().setExpandableContent(details);
        }
        alert.showAndWait();
    }
}
