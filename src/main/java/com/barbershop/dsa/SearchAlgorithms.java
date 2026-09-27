package com.barbershop.dsa;

import java.util.List;
import java.util.function.Predicate;

/**
 * Hand-written search algorithms.
 *
 * <p>DSA #4 of the project. The Customers screen uses them for lookup:</p>
 * <ul>
 *   <li>{@link #binarySearch(List, Object, java.util.Comparator)} — O(log n) exact lookup once
 *       the customer list has been sorted by name with {@link SortAlgorithms#mergeSort}.</li>
 *   <li>{@link #linearSearch(List, Predicate)} — O(n) filter for partial matches such as typing
 *       only the first few letters of a name or a phone number.</li>
 * </ul>
 */
public final class SearchAlgorithms {

    private SearchAlgorithms() {
        // utility class
    }

    /**
     * Linear search over an unsorted collection. O(n).
     *
     * @return the index of the first matching element, or {@code -1}
     */
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

    /**
     * Linear search that returns the element instead of the index. O(n).
     *
     * @return the first matching element, or {@code null}
     */
    public static <T> T findFirst(List<T> items, Predicate<? super T> match) {
        int index = linearSearch(items, match);
        return index < 0 ? null : items.get(index);
    }

    /**
     * Classic binary search using the elements' natural ordering. O(log n).
     *
     * <p>The list <strong>must</strong> already be sorted with the same ordering.</p>
     *
     * @return the index of {@code key}, or {@code -1} when not present
     */
    public static <T extends Comparable<? super T>> int binarySearch(List<T> sorted, T key) {
        return binarySearch(sorted, key, Comparable::compareTo);
    }

    /**
     * Binary search with an explicit comparator. O(log n).
     *
     * @return the index of {@code key}, or {@code -1} when not present
     */
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

    /**
     * Exact match helper: binary searches for {@code key} and returns the element itself.
     */
    public static <T extends Comparable<? super T>> T binaryFind(List<T> sorted, T key) {
        int index = binarySearch(sorted, key);
        return index < 0 ? null : sorted.get(index);
    }

    /**
     * Lower bound: index of the first element that is not less than {@code key}. O(log n).
     * Useful for showing "closest match" suggestions in the search box.
     */
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
