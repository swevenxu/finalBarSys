package com.barbershop.dsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class MyStack<T> {
    private static final int INITIAL_CAPACITY = 16;

    private Object[] elements;
    private int size;

    public MyStack() {
        this.elements = new Object[INITIAL_CAPACITY];
    }

    public void push(T item) {
        Objects.requireNonNull(item, "item");
        if (size == elements.length) {
            grow();
        }
        elements[size++] = item;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (size == 0) {
            throw new NoSuchElementException("Cannot pop an empty stack.");
        }
        T item = (T) elements[--size];
        elements[size] = null;
        return item;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) {
            throw new NoSuchElementException("Cannot peek an empty stack.");
        }
        return (T) elements[size - 1];
    }

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

    @SuppressWarnings("unchecked")
    public List<T> toList() {
        List<T> items = new ArrayList<>(size);
        for (int i = size - 1; i >= 0; i--) {
            items.add((T) elements[i]);
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    public List<T> toListBottomFirst() {
        List<T> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add((T) elements[i]);
        }
        return items;
    }

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
