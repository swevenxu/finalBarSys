package com.barbershop.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class Appointment {
    public static final String BOOKED = "Booked";
    public static final String WAITING = "Waiting";
    public static final String IN_PROGRESS = "In Progress";
    public static final String DONE = "Done";
    public static final String CANCELLED = "Cancelled";

    public static final String[] STATUSES = {BOOKED, WAITING, IN_PROGRESS, DONE, CANCELLED};

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private int appointmentId;
    private int customerId;
    private int barberId;
    private int serviceId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;

    private String customerName = "";
    private String barberName = "";
    private String serviceName = "";
    private double servicePrice;

    public static final Comparator<Appointment> BY_DATE_TIME =
            Comparator.comparing(Appointment::getAppointmentDate,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Appointment::getAppointmentTime,
                            Comparator.nullsLast(Comparator.naturalOrder()));

    public static final Comparator<Appointment> BY_DATE_TIME_DESC = BY_DATE_TIME.reversed();

    public Appointment() {
        this(0, 0, 0, 0, LocalDate.now(), LocalTime.now().withSecond(0).withNano(0), BOOKED);
    }

    public Appointment(int customerId, int barberId, int serviceId,
                       LocalDate appointmentDate, LocalTime appointmentTime, String status) {
        this(0, customerId, barberId, serviceId, appointmentDate, appointmentTime, status);
    }

    public Appointment(int appointmentId, int customerId, int barberId, int serviceId,
                       LocalDate appointmentDate, LocalTime appointmentTime, String status) {
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.barberId = barberId;
        this.serviceId = serviceId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status == null ? BOOKED : status;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getBarberId() {
        return barberId;
    }

    public void setBarberId(int barberId) {
        this.barberId = barberId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName == null ? "" : customerName;
    }

    public String getBarberName() {
        return barberName;
    }

    public void setBarberName(String barberName) {
        this.barberName = barberName == null ? "" : barberName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName == null ? "" : serviceName;
    }

    public double getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(double servicePrice) {
        this.servicePrice = servicePrice;
    }

    public String getFormattedTime() {
        return appointmentTime == null ? "" : appointmentTime.format(TIME_FORMAT);
    }

    public String getFormattedDate() {
        return appointmentDate == null ? "" : appointmentDate.format(DATE_FORMAT);
    }

    public java.time.LocalDateTime getDateTime() {
        if (appointmentDate == null || appointmentTime == null) {
            return null;
        }
        return java.time.LocalDateTime.of(appointmentDate, appointmentTime);
    }

    @Override
    public String toString() {
        return getFormattedDate() + " " + getFormattedTime() + " - " + customerName;
    }
}
