package com.barbershop.model;

import java.time.LocalDateTime;

public class QueueTicket {
    private int ticketId;
    private Customer customer;
    private Service service;
    private Barber barber;
    private LocalDateTime enqueuedAt;

    public QueueTicket(Customer customer, Service service, Barber barber) {
        this(0, customer, service, barber, LocalDateTime.now());
    }

    public QueueTicket(int ticketId, Customer customer, Service service, Barber barber) {
        this(ticketId, customer, service, barber, LocalDateTime.now());
    }

    public QueueTicket(int ticketId, Customer customer, Service service, Barber barber,
                       LocalDateTime enqueuedAt) {
        this.ticketId = ticketId;
        this.customer = customer;
        this.service = service;
        this.barber = barber;
        this.enqueuedAt = enqueuedAt;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Barber getBarber() {
        return barber;
    }

    public void setBarber(Barber barber) {
        this.barber = barber;
    }

    public LocalDateTime getEnqueuedAt() {
        return enqueuedAt;
    }

    public void setEnqueuedAt(LocalDateTime enqueuedAt) {
        this.enqueuedAt = enqueuedAt;
    }

    public String getCustomerName() {
        return customer == null ? "" : customer.getName();
    }

    public String getServiceName() {
        return service == null ? "" : service.getName();
    }

    public String getBarberName() {
        return barber == null ? "" : barber.getName();
    }

    public double getPrice() {
        return service == null ? 0.0 : service.getPrice();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QueueTicket other && customer != null && customer.equals(other.customer);
    }

    @Override
    public int hashCode() {
        return customer == null ? 0 : customer.hashCode();
    }

    @Override
    public String toString() {
        return getCustomerName() + " (" + getServiceName() + ")";
    }
}
