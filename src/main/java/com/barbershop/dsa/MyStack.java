package com.barbershop.dsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A hand-written LIFO stack backed by a growable array.
 *
 * <p>DSA #6 of the project. Completed payments are pushed here as they happen, which gives the
 * Transactions screen its "recent transactions" feed (newest on top) and powers
 * <em>Undo last payment</em>.</p>
 *
 * @param <T> element type
 */
public class MyStack<T> {

    private static final int INITIAL_CAPACITY = 16;

    private Object[] elements;
    private int size;

    public MyStack() {
        this.elements = new Object[INITIAL_CAPACITY];
    }

    /** Pushes an element onto the top of the stack. Amortised O(1). */
    public void push(T item) {
        Objects.requireNonNull(item, "item");
        if (size == elements.length) {
            grow();
        }
        elements[size++] = item;
    }

    /**
     * Removes and returns the element on top of the stack. Amortised O(1).
     *
     * @throws NoSuchElementException when the stack is empty
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (size == 0) {
            throw new NoSuchElementException("Cannot pop an empty stack.");
        }
        T item = (T) elements[--size];
        elements[size] = null;
        return item;
    }

    /**
     * Returns the top element without removing it. O(1).
     *
     * @throws NoSuchElementException when the stack is empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) {
            throw new NoSuchElementException("Cannot peek an empty stack.");
        }
        return (T) elements[size - 1];
    }

    /** Returns the top element, or {@code null} when the stack is empty. */
    public T peekOrNull() {
        return size == 0 ? null : peek();
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    /**
     * Snapshot with the top element first, which is the order the UI wants to display.
     * O(n).
     */
    @SuppressWarnings("unchecked")
    public List<T> toList() {
        List<T> items = new ArrayList<>(size);
        for (int i = size - 1; i >= 0; i--) {
            items.add((T) elements[i]);
        }
        return items;
    }

    /** Oldest first. The reverse of {@link #toList()}. */
    @SuppressWarnings("unchecked")
    public List<T> toListBottomFirst() {
        List<T> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add((T) elements[i]);
        }
        return items;
    }

    /** Replaces the current contents, used when reloading from the database. */
    public void loadFrom(Iterable<? extends T> items) {
        clear();
        for (T item : items) {
            push(item);
        }
    }

    private void grow() {
        int newCapacity = elements.length * 2;
        Object[] bigger = new Object[newCapacity];
        System.arraycopy(elements, 0, bigger, 0, size);
        elements = bigger;
    }

    @Override
    public String toString() {
        List<T> topFirst = toList();
        Collections.reverse(topFirst);
        StringBuilder sb = new StringBuilder("BOTTOM -> ");
        for (int i = 0; i < topFirst.size(); i++) {
            sb.append(topFirst.get(i));
            if (i < topFirst.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.append(" -> TOP").toString();
    }
}
