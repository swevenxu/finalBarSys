package com.barbershop.model;

/**
 * A service offered by the barbershop, for example "Haircut" at ₱150 for 30 minutes.
 */
public class Service implements Comparable<Service> {

    private int serviceId;
    private String name;
    private double price;
    private int durationMinutes;

    public Service() {
        this(0, "", 0.0, 0);
    }

    public Service(String name, double price, int durationMinutes) {
        this(0, name, price, durationMinutes);
    }

    public Service(int serviceId, String name, double price, int durationMinutes) {
        this.serviceId = serviceId;
        this.name = name;
        this.price = price;
        this.durationMinutes = durationMinutes;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    /** Combo boxes show the service name; tables build their own cell text. */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Service other) {
        return String.CASE_INSENSITIVE_ORDER.compare(
                name == null ? "" : name.trim(),
                other.name == null ? "" : other.name.trim());
    }
}
