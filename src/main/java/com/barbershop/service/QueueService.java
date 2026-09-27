package com.barbershop.service;

import com.barbershop.dsa.MyQueue;
import com.barbershop.model.Barber;
import com.barbershop.model.Customer;
import com.barbershop.model.QueueTicket;
import com.barbershop.model.Service;

import java.util.List;

public final class QueueService {
    private static final QueueService INSTANCE = new QueueService();

    private final MyQueue<QueueTicket> waitingLine = new MyQueue<>();
    private int nextTicketNumber = 1;

    private QueueService() { }

    public static QueueService getInstance() {
        return INSTANCE;
    }

    public QueueTicket enqueue(Customer customer, Service service, Barber barber) {
        QueueTicket ticket = new QueueTicket(nextTicketNumber++, customer, service, barber);
        waitingLine.enqueue(ticket);
        return ticket;
    }

    public QueueTicket serveNext() {
        return waitingLine.dequeue();
    }

    public QueueTicket peekNext() {
        return waitingLine.peekOrNull();
    }

    public boolean isEmpty() {
        return waitingLine.isEmpty();
    }

    public int size() {
        return waitingLine.size();
    }

    public boolean remove(QueueTicket ticket) {
        return waitingLine.remove(ticket);
    }

    public QueueTicket removeAt(int index) {
        return waitingLine.removeAt(index);
    }

    public List<QueueTicket> snapshot() {
        return waitingLine.toList();
    }

    public void clear() {
        waitingLine.clear();
    }

    public QueueTicket getAt(int index) {
        return waitingLine.get(index);
    }

    public MyQueue<QueueTicket> getWaitingLine() {
        return waitingLine;
    }
}
