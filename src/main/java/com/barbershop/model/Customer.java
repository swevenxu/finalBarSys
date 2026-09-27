package com.barbershop.model;

import java.util.Objects;

/**
 * A registered barbershop customer.
 *
 * <p>Implements {@link Comparable} by name so that customer lists can be sorted with the
 * project's own merge sort and then searched with binary search.</p>
 */
public class Customer implements Comparable<Customer> {

    private int customerId;
    private String name;
    private String phone;
    private String email;

    public Customer() {
        this(0, "", "", "");
    }

    public Customer(String name, String phone, String email) {
        this(0, name, phone, email);
    }

    public Customer(int customerId, String name, String phone, String email) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /** Customer names are compared case-insensitively, ignoring surrounding spaces. */
    @Override
    public int compareTo(Customer other) {
        return String.CASE_INSENSITIVE_ORDER.compare(safe(name), safe(other.name));
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Customer other)) {
            return false;
        }
        if (customerId != 0 && other.customerId != 0) {
            return customerId == other.customerId;
        }
        return Objects.equals(safe(name).toLowerCase(), safe(other.name).toLowerCase())
                && Objects.equals(safe(phone), safe(other.phone));
    }

    @Override
    public int hashCode() {
        return customerId != 0 ? Integer.hashCode(customerId) : safe(name).toLowerCase().hashCode();
    }

    /** Table views show the customer name directly. */
    @Override
    public String toString() {
        return name;
    }
}
