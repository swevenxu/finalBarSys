package com.barbershop.dsa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class MyPriorityQueue<T> {
    private final Comparator<? super T> comparator;
    private final List<T> heap = new ArrayList<>();

    public MyPriorityQueue(Comparator<? super T> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "comparator");
    }

    public void add(T item) {
        insert(item);
    }

    public void insert(T item) {
        Objects.requireNonNull(item, "item");
        heap.add(item);
        siftUp(heap.size() - 1);
    }

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

    public T peek() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Cannot peek an empty priority queue.");
        }
        return heap.get(0);
    }

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

    public List<T> toSortedList() {
        MyPriorityQueue<T> copy = new MyPriorityQueue<>(comparator);
        copy.heap.addAll(heap);
        List<T> sorted = new ArrayList<>(heap.size());
        while (!copy.isEmpty()) {
            sorted.add(copy.poll());
        }
        return sorted;
    }

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
