package com.barbershop.dsa;

import java.util.List;
import java.util.function.Predicate;

public final class SearchAlgorithms {
    private SearchAlgorithms() { }

    public static <T> int linearSearch(List<T> items, Predicate<? super T> match) {
        if (items == null || match == null) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            if (match.test(items.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static <T> T findFirst(List<T> items, Predicate<? super T> match) {
        int index = linearSearch(items, match);
        return index < 0 ? null : items.get(index);
    }

    public static <T extends Comparable<? super T>> int binarySearch(List<T> sorted, T key) {
        return binarySearch(sorted, key, Comparable::compareTo);
    }

    public static <T> int binarySearch(List<T> sorted, T key, java.util.Comparator<? super T> comparator) {
        if (sorted == null || sorted.isEmpty() || key == null) {
            return -1;
        }
        int low = 0;
        int high = sorted.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int comparison = comparator.compare(sorted.get(mid), key);
            if (comparison == 0) {
                return mid;
            }
            if (comparison < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    public static <T extends Comparable<? super T>> T binaryFind(List<T> sorted, T key) {
        int index = binarySearch(sorted, key);
        return index < 0 ? null : sorted.get(index);
    }

    public static <T extends Comparable<? super T>> int lowerBound(List<T> sorted, T key) {
        int low = 0;
        int high = sorted.size();
        while (low < high) {
            int mid = low + (high - low) / 2;
            if (sorted.get(mid).compareTo(key) < 0) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }
}
