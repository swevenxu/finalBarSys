package com.barbershop.dsa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class SortAlgorithms {
    private SortAlgorithms() { }

    public static <T extends Comparable<? super T>> void mergeSort(List<T> list) {
        mergeSort(list, Comparator.naturalOrder());
    }

    public static <T> void mergeSort(List<T> list, Comparator<? super T> comparator) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(comparator, "comparator");
        if (list.size() < 2) {
            return;
        }
        List<T> buffer = new ArrayList<>(list);
        mergeSortRange(buffer, list, 0, list.size() - 1, comparator);
    }

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

    private static <T> void merge(List<T> source, List<T> target,
                                  int low, int mid, int high,
                                  Comparator<? super T> comparator) {
        int i = low;
        int j = mid + 1;
        int k = low;

        while (i <= mid && j <= high) {
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

    public static <T extends Comparable<? super T>> void insertionSort(List<T> list) {
        insertionSort(list, Comparator.naturalOrder());
    }

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

    public static <T> List<T> sortedCopy(List<T> list, Comparator<? super T> comparator) {
        List<T> copy = new ArrayList<>(list);
        mergeSort(copy, comparator);
        return copy;
    }
}
