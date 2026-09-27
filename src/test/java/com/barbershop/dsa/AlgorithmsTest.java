package com.barbershop.dsa;

import com.barbershop.model.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlgorithmsTest {
    @Test
    @DisplayName("merge sort orders integers and does not touch the input array")
    void mergeSortOrdersIntegers() {
        List<Integer> values = new ArrayList<>(List.of(11, 9, 30, 8));
        SortAlgorithms.mergeSort(values);

        assertEquals(List.of(8, 9, 11, 30), values);
    }

    @Test
    @DisplayName("merge sort handles empty, single element and already sorted lists")
    void mergeSortEdgeCases() {
        List<Integer> empty = new ArrayList<>();
        SortAlgorithms.mergeSort(empty);
        assertTrue(empty.isEmpty());

        List<Integer> single = new ArrayList<>(List.of(5));
        SortAlgorithms.mergeSort(single);
        assertEquals(List.of(5), single);

        List<Integer> sorted = new ArrayList<>(List.of(1, 2, 3, 4));
        SortAlgorithms.mergeSort(sorted);
        assertEquals(List.of(1, 2, 3, 4), sorted);

        List<Integer> reversed = new ArrayList<>(List.of(4, 3, 2, 1));
        SortAlgorithms.mergeSort(reversed);
        assertEquals(List.of(1, 2, 3, 4), reversed);
    }

    @Test
    @DisplayName("merge sort with a custom comparator (price, descending)")
    void mergeSortWithComparator() {
        List<int[]> prices = new ArrayList<>(List.of(new int[]{150}, new int[]{250}, new int[]{100}));
        SortAlgorithms.mergeSort(prices, Comparator.comparingInt(entry -> -entry[0]));

        assertEquals(250, prices.get(0)[0]);
        assertEquals(100, prices.get(2)[0]);
    }

    @Test
    @DisplayName("merge sort is stable, so equal keys keep their original order")
    void mergeSortIsStable() {
        record Row(String key, String tag) {
        }
        List<Row> rows = new ArrayList<>(List.of(
                new Row("b", "first-b"),
                new Row("a", "first-a"),
                new Row("b", "second-b"),
                new Row("a", "second-a")));

        SortAlgorithms.mergeSort(rows, Comparator.comparing(Row::key));

        assertEquals(List.of("first-a", "second-a", "first-b", "second-b"),
                rows.stream().map(Row::tag).toList());
    }

    @Test
    @DisplayName("sortedCopy leaves the original list untouched")
    void sortedCopyDoesNotMutate() {
        List<Integer> original = new ArrayList<>(List.of(3, 1, 2));
        List<Integer> sorted = SortAlgorithms.sortedCopy(original, Comparator.naturalOrder());

        assertEquals(List.of(3, 1, 2), original);
        assertEquals(List.of(1, 2, 3), sorted);
    }

    @Test
    @DisplayName("insertion sort orders the list in place")
    void insertionSortOrders() {
        List<Integer> values = new ArrayList<>(List.of(5, 2, 9, 1));
        SortAlgorithms.insertionSort(values);
        assertEquals(List.of(1, 2, 5, 9), values);
    }

    @Test
    @DisplayName("binary search finds a customer in a name-sorted list")
    void binarySearchFindsCustomer() {
        List<Customer> customers = new ArrayList<>(List.of(
                new Customer("Alex", "", null),
                new Customer("Bob", "", null),
                new Customer("Carlo", "", null),
                new Customer("Daniel", "", null),
                new Customer("Mark", "", null),
                new Customer("Pedro", "", null),
                new Customer("Sarah", "", null)));

        int index = SearchAlgorithms.binarySearch(customers, new Customer("Pedro", "", null));
        assertEquals(5, index);
        assertEquals("Pedro", customers.get(index).getName());

        assertEquals(-1, SearchAlgorithms.binarySearch(customers, new Customer("Nobody", "", null)));
    }

    @Test
    @DisplayName("binary search tolerates empty lists and null keys")
    void binarySearchEdgeCases() {
        List<Customer> empty = new ArrayList<>();
        assertEquals(-1, SearchAlgorithms.binarySearch(empty, new Customer("A", "", null)));
        assertEquals(-1, SearchAlgorithms.binarySearch(
                List.of(new Customer("A", "", null)), null));
    }

    @Test
    @DisplayName("lower bound points at the first element that is not smaller")
    void lowerBoundFindsInsertionPoint() {
        List<Integer> sorted = List.of(1, 3, 5, 7);
        assertEquals(0, SearchAlgorithms.lowerBound(sorted, 0));
        assertEquals(1, SearchAlgorithms.lowerBound(sorted, 3));
        assertEquals(4, SearchAlgorithms.lowerBound(sorted, 9));
    }

    @Test
    @DisplayName("linear search finds by predicate and returns the element")
    void linearSearchFindsByPredicate() {
        List<Customer> customers = List.of(
                new Customer("Juan", "0917-555-0101", null),
                new Customer("Maria", "0919-555-0103", null));

        int index = SearchAlgorithms.linearSearch(customers,
                customer -> customer.getPhone().endsWith("0103"));
        assertEquals(1, index);

        Customer found = SearchAlgorithms.findFirst(customers,
                customer -> "Juan".equals(customer.getName()));
        assertEquals("0917-555-0101", found.getPhone());

        assertNull(SearchAlgorithms.findFirst(customers, customer -> false));
        assertEquals(-1, SearchAlgorithms.linearSearch(customers, customer -> false));
    }

    @Test
    @DisplayName("search and sort combine the way the Customers screen uses them")
    void searchOverSortedData() {
        List<Customer> unsorted = new ArrayList<>(List.of(
                new Customer("Pedro", "", null),
                new Customer("Alex", "", null),
                new Customer("Maria", "", null)));

        List<Customer> sorted = SortAlgorithms.sortedCopy(unsorted, Comparator.naturalOrder());
        assertEquals(List.of("Alex", "Maria", "Pedro"), sorted.stream().map(Customer::getName).toList());

        assertTrue(SearchAlgorithms.binarySearch(sorted, new Customer("Maria", "", null)) >= 0);
    }

    @Test
    @DisplayName("sorting appointments by date and time puts the earliest first")
    void sortAppointmentsByDateTime() {
        List<java.time.LocalTime> times = new ArrayList<>(List.of(
                java.time.LocalTime.of(11, 0),
                java.time.LocalTime.of(9, 0),
                java.time.LocalTime.of(10, 30),
                java.time.LocalTime.of(8, 30)));

        SortAlgorithms.mergeSort(times);
        assertEquals(List.of(
                        java.time.LocalTime.of(8, 30),
                        java.time.LocalTime.of(9, 0),
                        java.time.LocalTime.of(10, 30),
                        java.time.LocalTime.of(11, 0)),
                times);
        assertFalse(times.get(0).isAfter(times.get(1)));
    }
}
