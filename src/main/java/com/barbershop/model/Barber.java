package com.barbershop.model;

/**
 * A barber employed by the shop.
 *
 * <p>Status is stored as one of {@link #AVAILABLE}, {@link #BUSY} or {@link #OFF_DUTY} so the
 * Queue screen can show who is free to take the next walk-in.</p>
 */
public class Barber implements Comparable<Barber> {

    public static final String AVAILABLE = "Available";
    public static final String BUSY = "Busy";
    public static final String OFF_DUTY = "Off Duty";

    public static final String[] STATUSES = {AVAILABLE, BUSY, OFF_DUTY};

    private int barberId;
    private String name;
    private String specialization;
    private String status;

    public Barber() {
        this(0, "", "", AVAILABLE);
    }

    public Barber(String name, String specialization, String status) {
        this(0, name, specialization, status);
    }

    public Barber(int barberId, String name, String specialization, String status) {
        this.barberId = barberId;
        this.name = name;
        this.specialization = specialization;
        this.status = status == null || status.isBlank() ? AVAILABLE : status;
    }

    public int getBarberId() {
        return barberId;
    }

    public void setBarberId(int barberId) {
        this.barberId = barberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return AVAILABLE.equalsIgnoreCase(status);
    }

    /** Used to load combo boxes. */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Barber other) {
        return String.CASE_INSENSITIVE_ORDER.compare(
                name == null ? "" : name.trim(),
                other.name == null ? "" : other.name.trim());
    }
}
