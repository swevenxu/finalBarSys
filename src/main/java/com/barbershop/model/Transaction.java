package com.barbershop.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    public static final String CASH = "Cash";
    public static final String CARD = "Card";
    public static final String GCASH = "GCash";

    public static final String[] PAYMENT_METHODS = {CASH, CARD, GCASH};

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private int transactionId;
    private int appointmentId;
    private double amount;
    private String paymentMethod;
    private LocalDateTime transactionDate;

    private String customerName = "";
    private String serviceName = "";

    public Transaction() {
        this(0, 0, 0.0, CASH, LocalDateTime.now());
    }

    public Transaction(int appointmentId, double amount, String paymentMethod) {
        this(0, appointmentId, amount, paymentMethod, LocalDateTime.now());
    }

    public Transaction(int transactionId, int appointmentId, double amount,
                       String paymentMethod, LocalDateTime transactionDate) {
        this.transactionId = transactionId;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionDate = transactionDate;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName == null ? "" : customerName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName == null ? "" : serviceName;
    }

    public String getFormattedDate() {
        return transactionDate == null ? "" : transactionDate.format(DATE_FORMAT);
    }

    @Override
    public String toString() {
        return "#" + transactionId + " " + customerName + " " + getFormattedDate();
    }
}
