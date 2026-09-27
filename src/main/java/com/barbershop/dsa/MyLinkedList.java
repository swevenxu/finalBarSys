package com.barbershop.dsa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class MyLinkedList<T> implements Iterable<T> {
    public static final class Node<E> {
        private E data;
        private Node<E> next;

        public Node(E data) {
            this.data = data;
        }

        public E getData() {
            return data;
        }

        public Node<E> getNext() {
            return next;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void addFirst(T item) {
        Objects.requireNonNull(item, "item");
        Node<T> node = new Node<>(item);
        node.next = head;
        head = node;
        if (tail == null) {
            tail = node;
        }
        size++;
    }

    public void addLast(T item) {
        Objects.requireNonNull(item, "item");
        Node<T> node = new Node<>(item);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public void add(T item) {
        addLast(item);
    }

    public void add(int index, T item) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        if (index == 0) {
            addFirst(item);
            return;
        }
        if (index == size) {
            addLast(item);
            return;
        }
        Node<T> previous = nodeAt(index - 1);
        Node<T> node = new Node<>(Objects.requireNonNull(item, "item"));
        node.next = previous.next;
        previous.next = node;
        size++;
    }

    public T get(int index) {
        return nodeAt(index).data;
    }

    public void set(int index, T item) {
        nodeAt(index).data = Objects.requireNonNull(item, "item");
    }

    public T removeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        if (index == 0) {
            T data = head.data;
            head = head.next;
            if (head == null) {
                tail = null;
            }
            size--;
            return data;
        }
        Node<T> previous = nodeAt(index - 1);
        Node<T> target = previous.next;
        previous.next = target.next;
        if (target == tail) {
            tail = previous;
        }
        size--;
        return target.data;
    }

    public boolean remove(T item) {
        Node<T> previous = null;
        Node<T> current = head;
        while (current != null) {
            if (Objects.equals(current.data, item)) {
                if (previous == null) {
                    head = current.next;
                } else {
                    previous.next = current.next;
                }
                if (current == tail) {
                    tail = previous;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    public boolean contains(T item) {
        for (Node<T> current = head; current != null; current = current.next) {
            if (Objects.equals(current.data, item)) {
                return true;
            }
        }
        return false;
    }

    public T getFirst() {
        if (head == null) {
            throw new NoSuchElementException("The list is empty.");
        }
        return head.data;
    }

    public T getLast() {
        if (tail == null) {
            throw new NoSuchElementException("The list is empty.");
        }
        return tail.data;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    public Node<T> getHead() {
        return head;
    }

    public List<T> toList() {
        List<T> items = new ArrayList<>(size);
        for (Node<T> current = head; current != null; current = current.next) {
            items.add(current.data);
        }
        return items;
    }

    public void reverse() {
        Node<T> previous = null;
        Node<T> current = head;
        tail = head;
        while (current != null) {
            Node<T> next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }
        head = previous;
    }

    private Node<T> nodeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = head;

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
        StringBuilder sb = new StringBuilder("HEAD -> ");
        for (Node<T> current = head; current != null; current = current.next) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(" -> ");
            }
        }
        return sb.append(" -> null").toString();
    }
}
