package com.barbershop.controller;

import com.barbershop.model.Appointment;
import javafx.scene.control.TableCell;

public class StatusCell extends TableCell<Appointment, String> {
    @Override
    protected void updateItem(String status, boolean empty) {
        super.updateItem(status, empty);

        getStyleClass().removeAll("status-done", "status-waiting", "status-booked",
                "status-cancelled", "status-progress");

        if (empty || status == null) {
            setText(null);
            return;
        }

        setText(status);
        switch (status) {
            case Appointment.DONE -> getStyleClass().add("status-done");
            case Appointment.WAITING -> getStyleClass().add("status-waiting");
            case Appointment.IN_PROGRESS -> getStyleClass().add("status-progress");
            case Appointment.CANCELLED -> getStyleClass().add("status-cancelled");
            default -> getStyleClass().add("status-booked");
        }
    }
}
