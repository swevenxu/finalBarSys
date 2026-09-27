package com.barbershop.dsa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A hand-written FIFO queue.
 *
 * <p>DSA #1 of the project. Walk-in customers are enqueued at the rear and served from the
 * front, so the first customer to arrive is the first to be served.</p>
 *
 * <p>Backed by singly linked nodes, giving O(1) {@link #enqueue(Object)} and
 * {@link #dequeue()} because a {@code rear} pointer is kept alongside {@code front}.</p>
 *
 * @param <T> element type
 */
public class MyQueue<T> implements Iterable<T> {

    /** One link in the chain. */
    private static final class Node<E> {
        private final E data;
        private Node<E> next;

        private Node(E data) {
            this.data = data;
        }
    }

    private Node<T> front;
    private Node<T> rear;
    private int size;

    /**
     * Adds an element to the rear of the queue. O(1).
     *
     * @return {@code true} when the element was added
     */
    public boolean enqueue(T item) {
        Objects.requireNonNull(item, "item");
        Node<T> node = new Node<>(item);
        if (rear == null) {
            front = node;
            rear = node;
        } else {
            rear.next = node;
            rear = node;
        }
        size++;
        return true;
    }

    /**
     * Removes and returns the element at the front of the queue. O(1).
     *
     * @throws NoSuchElementException when the queue is empty
     */
    public T dequeue() {
        if (front == null) {
            throw new NoSuchElementException("Cannot dequeue from an empty queue.");
        }
        T data = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        size--;
        return data;
    }

    /**
     * Returns the element at the front without removing it. O(1).
     *
     * @throws NoSuchElementException when the queue is empty
     */
    public T peek() {
        if (front == null) {
            throw new NoSuchElementException("Cannot peek an empty queue.");
        }
        return front.data;
    }

    /** Returns the front element, or {@code null} when the queue is empty. */
    public T peekOrNull() {
        return front == null ? null : front.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    /**
     * Removes the first occurrence of {@code item}. O(n) — needed when the receptionist
     * cancels somebody who is not at the front of the line.
     *
     * @return {@code true} when an element was removed
     */
    public boolean remove(T item) {
        Node<T> previous = null;
        Node<T> current = front;
        while (current != null) {
            if (Objects.equals(current.data, item)) {
                if (previous == null) {
                    front = current.next;
                } else {
                    previous.next = current.next;
                }
                if (current == rear) {
                    rear = previous;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    /** Removes and returns the element at position {@code index} (0 = front). O(n). */
    public T removeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        Node<T> previous = null;
        Node<T> current = front;
        for (int i = 0; i < index; i++) {
            previous = current;
            current = current.next;
        }
        if (previous == null) {
            front = current.next;
        } else {
            previous.next = current.next;
        }
        if (current == rear) {
            rear = previous;
        }
        size--;
        return current.data;
    }

    /** Returns the element at position {@code index} (0 = front) without removing it. O(n). */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        Node<T> current = front;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    /** Snapshot of the queue from front to rear. O(n). */
    public List<T> toList() {
        List<T> items = new ArrayList<>(size);
        for (Node<T> current = front; current != null; current = current.next) {
            items.add(current.data);
        }
        return items;
    }

    /** Walks the queue from front to rear without modifying it. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = front;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FRONT -> ");
        for (Node<T> current = front; current != null; current = current.next) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(" -> ");
            }
        }
        return sb.append(" -> REAR").toString();
    }
}
