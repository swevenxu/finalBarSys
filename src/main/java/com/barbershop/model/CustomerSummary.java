package com.barbershop.model;

import com.barbershop.util.Money;

public class CustomerSummary {
    private final Customer customer;
    private final int appointmentCount;
    private final double totalSpent;

    public CustomerSummary(Customer customer, int appointmentCount, double totalSpent) {
        this.customer = customer;
        this.appointmentCount = appointmentCount;
        this.totalSpent = totalSpent;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getAppointmentCount() {
        return appointmentCount;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public String getFormattedSpent() {
        return Money.format(totalSpent);
    }

    public String getName() {
        return customer.getName();
    }

    public String getPhone() {
        return customer.getPhone();
    }

    public String getEmail() {
        return customer.getEmail();
    }
}
