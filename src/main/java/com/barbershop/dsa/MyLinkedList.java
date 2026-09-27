package com.barbershop.dsa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A hand-written singly linked list.
 *
 * <p>DSA #5 of the project. A customer's service history is kept as a chain of nodes so that
 * the newest appointment sits at the head and the older ones are reached by following
 * {@code next} pointers, exactly like the diagram in the project plan:</p>
 *
 * <pre>
 * HEAD
 *  ↓
 * [Haircut — Sept 20] → [Haircut + Beard — Sept 22] → [Haircut — Sept 24] → null
 * </pre>
 *
 * @param <T> element type
 */
public class MyLinkedList<T> implements Iterable<T> {

    /** The {@code Node} holding one element and a pointer to the next node. */
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

    /** Inserts at the head. O(1). Used to push a newly completed appointment onto the history. */
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

    /** Appends at the tail. O(1) because the tail pointer is kept up to date. */
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

    /** Alias for {@link #addLast(Object)}, so the list reads like a normal collection. */
    public void add(T item) {
        addLast(item);
    }

    /** Inserts at {@code index}, shifting the rest of the chain. O(n). */
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

    /** Returns the element at {@code index}. O(n). */
    public T get(int index) {
        return nodeAt(index).data;
    }

    /** Replaces the element at {@code index}. O(n). */
    public void set(int index, T item) {
        nodeAt(index).data = Objects.requireNonNull(item, "item");
    }

    /** Removes the element at {@code index}. O(n). */
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

    /** Removes the first occurrence of {@code item}. O(n). */
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

    /** Linear scan through the chain. O(n). */
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

    /** Returns the head node so callers can demonstrate pointer traversal. */
    public Node<T> getHead() {
        return head;
    }

    /** Copies the chain into a list, head first. O(n). */
    public List<T> toList() {
        List<T> items = new ArrayList<>(size);
        for (Node<T> current = head; current != null; current = current.next) {
            items.add(current.data);
        }
        return items;
    }

    /**
     * Reverses the chain in place by re-pointing every {@code next} pointer. O(n).
     */
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

    /** Follows {@code next} pointers from the head. */
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
