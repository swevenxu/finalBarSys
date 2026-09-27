package com.barbershop.dsa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Hand-written sorting algorithms.
 *
 * <p>DSA #3 of the project. The Appointments screen sorts the table with
 * {@link #mergeSort(List, Comparator)} — by time, customer, barber, service or price — and the
 * Customers screen sorts by name before running a binary search.</p>
 *
 * <p>Merge sort is O(n log n) in the best, average and worst case, and it is stable, so rows
 * that compare equal keep their previous order.</p>
 */
public final class SortAlgorithms {

    private SortAlgorithms() {
        // utility class
    }

    /**
     * Sorts the list in place with merge sort using natural ordering.
     */
    public static <T extends Comparable<? super T>> void mergeSort(List<T> list) {
        mergeSort(list, Comparator.naturalOrder());
    }

    /**
     * Sorts the list in place with merge sort using {@code comparator}. O(n log n).
     *
     * <p>The list is copied into an array, split down the middle recursively, and the sorted
     * halves are merged back together in linear time.</p>
     */
    public static <T> void mergeSort(List<T> list, Comparator<? super T> comparator) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(comparator, "comparator");
        if (list.size() < 2) {
            return;
        }
        List<T> buffer = new ArrayList<>(list);
        mergeSortRange(buffer, list, 0, list.size() - 1, comparator);
    }

    /**
     * Recursive divide step. {@code source} holds the current working data and {@code target}
     * receives the merged result.
     */
    private static <T> void mergeSortRange(List<T> source, List<T> target,
                                           int low, int high,
                                           Comparator<? super T> comparator) {
        if (low >= high) {
            return;
        }
        int mid = low + (high - low) / 2;
        mergeSortRange(target, source, low, mid, comparator);
        mergeSortRange(target, source, mid + 1, high, comparator);
        merge(source, target, low, mid, high, comparator);
    }

    /** Merges two adjacent sorted runs of {@code source} into {@code target}. O(n). */
    private static <T> void merge(List<T> source, List<T> target,
                                  int low, int mid, int high,
                                  Comparator<? super T> comparator) {
        int i = low;
        int j = mid + 1;
        int k = low;

        while (i <= mid && j <= high) {
            // "<=" keeps the sort stable.
            if (comparator.compare(source.get(i), source.get(j)) <= 0) {
                target.set(k++, source.get(i++));
            } else {
                target.set(k++, source.get(j++));
            }
        }
        while (i <= mid) {
            target.set(k++, source.get(i++));
        }
        while (j <= high) {
            target.set(k++, source.get(j++));
        }
    }

    /**
     * Insertion sort — included as the simple O(n²) counterpart and used for the small,
     * nearly sorted queue snapshots.
     */
    public static <T extends Comparable<? super T>> void insertionSort(List<T> list) {
        insertionSort(list, Comparator.naturalOrder());
    }

    /** Insertion sort with an explicit comparator. O(n²). */
    public static <T> void insertionSort(List<T> list, Comparator<? super T> comparator) {
        for (int i = 1; i < list.size(); i++) {
            T key = list.get(i);
            int j = i - 1;
            while (j >= 0 && comparator.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    /**
     * Returns a new sorted copy, leaving the original list untouched.
     */
    public static <T> List<T> sortedCopy(List<T> list, Comparator<? super T> comparator) {
        List<T> copy = new ArrayList<>(list);
        mergeSort(copy, comparator);
        return copy;
    }
}
