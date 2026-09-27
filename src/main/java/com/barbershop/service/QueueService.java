package com.barbershop.service;

import com.barbershop.dsa.MyQueue;
import com.barbershop.model.Barber;
import com.barbershop.model.Customer;
import com.barbershop.model.QueueTicket;
import com.barbershop.model.Service;

import java.util.List;

/**
 * Business logic for the walk-in line.
 *
 * <p>This is where DSA #1 lives in the running system: the waiting customers are held in the
 * project's own {@link MyQueue}, so adding a walk-in is an {@code enqueue} and serving the next
 * customer is a {@code dequeue}. The queue lives in memory for the duration of the shift —
 * walk-in lines do not outlive the working day.</p>
 */
public final class QueueService {

    private static final QueueService INSTANCE = new QueueService();

    private final MyQueue<QueueTicket> waitingLine = new MyQueue<>();
    private int nextTicketNumber = 1;

    private QueueService() {
        // singleton
    }

    public static QueueService getInstance() {
        return INSTANCE;
    }

    /** Adds a walk-in customer to the rear of the line. O(1). */
    public QueueTicket enqueue(Customer customer, Service service, Barber barber) {
        QueueTicket ticket = new QueueTicket(nextTicketNumber++, customer, service, barber);
        waitingLine.enqueue(ticket);
        return ticket;
    }

    /** Removes and returns the customer at the front of the line. O(1). */
    public QueueTicket serveNext() {
        return waitingLine.dequeue();
    }

    /** The customer who is next to be served, or {@code null} when nobody is waiting. */
    public QueueTicket peekNext() {
        return waitingLine.peekOrNull();
    }

    public boolean isEmpty() {
        return waitingLine.isEmpty();
    }

    public int size() {
        return waitingLine.size();
    }

    /** Removes a specific ticket, for example when the customer leaves before being served. */
    public boolean remove(QueueTicket ticket) {
        return waitingLine.remove(ticket);
    }

    /** Removes the ticket at {@code index} (0 = front). */
    public QueueTicket removeAt(int index) {
        return waitingLine.removeAt(index);
    }

    /** Snapshot of the line from front to rear. O(n). */
    public List<QueueTicket> snapshot() {
        return waitingLine.toList();
    }

    /** Empties the line, for example at the end of the day. */
    public void clear() {
        waitingLine.clear();
    }

    public QueueTicket getAt(int index) {
        return waitingLine.get(index);
    }

    /** Exposes the underlying structure so the Queue screen can demonstrate front/rear. */
    public MyQueue<QueueTicket> getWaitingLine() {
        return waitingLine;
    }
}
