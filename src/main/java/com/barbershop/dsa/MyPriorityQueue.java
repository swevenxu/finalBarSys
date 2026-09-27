package com.barbershop.dsa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A hand-written priority queue implemented as a binary min-heap.
 *
 * <p>DSA #2 of the project. The dashboard uses it to keep the day's appointments ordered by
 * appointment time, so the "next up" customer is always at the top of the heap.</p>
 *
 * <p>Insert and poll are O(log n); peek is O(1). The heap is stored in an array where the
 * children of index {@code i} live at {@code 2i + 1} and {@code 2i + 2}.</p>
 *
 * @param <T> element type
 */
public class MyPriorityQueue<T> {

    private final Comparator<? super T> comparator;
    private final List<T> heap = new ArrayList<>();

    public MyPriorityQueue(Comparator<? super T> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "comparator");
    }

    /** Alias for {@link #insert(Object)} matching the plain-English name. */
    public void add(T item) {
        insert(item);
    }

    /** Inserts an element and restores the heap property by sifting up. O(log n). */
    public void insert(T item) {
        Objects.requireNonNull(item, "item");
        heap.add(item);
        siftUp(heap.size() - 1);
    }

    /**
     * Removes and returns the smallest element (the highest priority). O(log n).
     *
     * @throws NoSuchElementException when empty
     */
    public T poll() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Cannot poll an empty priority queue.");
        }
        T top = heap.get(0);
        int lastIndex = heap.size() - 1;
        T last = heap.remove(lastIndex);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return top;
    }

    /**
     * Returns the smallest element without removing it. O(1).
     *
     * @throws NoSuchElementException when empty
     */
    public T peek() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Cannot peek an empty priority queue.");
        }
        return heap.get(0);
    }

    /** Returns the highest priority element, or {@code null} when empty. */
    public T peekOrNull() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }

    public void clear() {
        heap.clear();
    }

    /**
     * Removes the highest priority elements one at a time and returns them in priority order.
     * Uses a defensive copy so the heap is left untouched. O(n log n).
     */
    public List<T> toSortedList() {
        MyPriorityQueue<T> copy = new MyPriorityQueue<>(comparator);
        copy.heap.addAll(heap);
        List<T> sorted = new ArrayList<>(heap.size());
        while (!copy.isEmpty()) {
            sorted.add(copy.poll());
        }
        return sorted;
    }

    /** Heap order (not fully sorted) snapshot — useful for showing "everything in the heap". */
    public List<T> toList() {
        return new ArrayList<>(heap);
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (compare(heap.get(index), heap.get(parent)) >= 0) {
                break;
            }
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        int size = heap.size();
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;

            if (left < size && compare(heap.get(left), heap.get(smallest)) < 0) {
                smallest = left;
            }
            if (right < size && compare(heap.get(right), heap.get(smallest)) < 0) {
                smallest = right;
            }
            if (smallest == index) {
                return;
            }
            swap(index, smallest);
            index = smallest;
        }
    }

    private int compare(T a, T b) {
        return comparator.compare(a, b);
    }

    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    @Override
    public String toString() {
        return "PriorityQueue(size=" + heap.size() + ", top=" + peekOrNull() + ")";
    }
}
