package com.barbershop.dsa;

import com.barbershop.model.Appointment;
import com.barbershop.model.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the hand-written data structures in {@code com.barbershop.dsa}. */
class DataStructuresTest {

    @Nested
    @DisplayName("MyQueue (FIFO walk-in line)")
    class QueueTests {

        @Test
        void servesCustomersInArrivalOrder() {
            MyQueue<String> queue = new MyQueue<>();
            queue.enqueue("Juan");
            queue.enqueue("Pedro");
            queue.enqueue("Maria");

            assertEquals(3, queue.size());
            assertEquals("Juan", queue.peek(), "peek returns the FRONT without removing it");

            assertEquals("Juan", queue.dequeue());
            assertEquals("Pedro", queue.dequeue());
            assertEquals(1, queue.size());
            assertEquals("Maria", queue.dequeue());
            assertTrue(queue.isEmpty());
        }

        @Test
        void throwsWhenDequeuingEmptyQueue() {
            MyQueue<String> queue = new MyQueue<>();
            assertThrows(NoSuchElementException.class, queue::dequeue);
            assertThrows(NoSuchElementException.class, queue::peek);
            assertNull(queue.peekOrNull());
        }

        @Test
        void canRemoveFromTheMiddle() {
            MyQueue<String> queue = new MyQueue<>();
            queue.enqueue("A");
            queue.enqueue("B");
            queue.enqueue("C");

            assertTrue(queue.remove("B"));
            assertEquals(List.of("A", "C"), queue.toList());
            assertEquals(2, queue.size());

            assertEquals("C", queue.removeAt(1));
            assertEquals(List.of("A"), queue.toList());
            assertFalse(queue.remove("Z"));
        }

        @Test
        void iterationGoesFrontToRear() {
            MyQueue<Integer> queue = new MyQueue<>();
            queue.enqueue(1);
            queue.enqueue(2);
            queue.enqueue(3);

            List<Integer> visited = new ArrayList<>();
            for (Integer value : queue) {
                visited.add(value);
            }
            assertEquals(List.of(1, 2, 3), visited);
        }
    }

    @Nested
    @DisplayName("MyPriorityQueue (min-heap)")
    class PriorityQueueTests {

        @Test
        void pollsSmallestFirst() {
            MyPriorityQueue<Integer> heap = new MyPriorityQueue<>(Comparator.naturalOrder());
            for (int value : new int[]{5, 1, 9, 3, 7, 2}) {
                heap.insert(value);
            }

            assertEquals(1, heap.peek());
            assertEquals(List.of(1, 2, 3, 5, 7, 9), heap.toSortedList());
        }

        @Test
        void respectsACustomComparator() {
            MyPriorityQueue<String> byLength =
                    new MyPriorityQueue<>(Comparator.comparingInt(String::length));
            byLength.add("banana");
            byLength.add("kiwi");
            byLength.add("fig");

            assertEquals("fig", byLength.peek());
            assertEquals(List.of("fig", "kiwi", "banana"), byLength.toSortedList());
        }

        @Test
        void throwsWhenEmpty() {
            MyPriorityQueue<Integer> heap = new MyPriorityQueue<>(Comparator.naturalOrder());
            assertTrue(heap.isEmpty());
            assertNull(heap.peekOrNull());
            assertThrows(NoSuchElementException.class, heap::poll);
        }

        @Test
        void surfacesTheEarliestAppointment() {
            MyPriorityQueue<Appointment> pending =
                    new MyPriorityQueue<>(Appointment.BY_DATE_TIME);

            pending.insert(appointment("Late", LocalTime.of(11, 0)));
            pending.insert(appointment("Early", LocalTime.of(8, 30)));
            pending.insert(appointment("Middle", LocalTime.of(10, 0)));

            assertEquals("Early", pending.peek().getCustomerName());
        }

        private Appointment appointment(String customer, LocalTime time) {
            Appointment appointment =
                    new Appointment(1, 1, 1, LocalDate.now(), time, Appointment.BOOKED);
            appointment.setCustomerName(customer);
            return appointment;
        }
    }

    @Nested
    @DisplayName("MyLinkedList (service history)")
    class LinkedListTests {

        @Test
        void supportsBothEndsAndTraversal() {
            MyLinkedList<String> list = new MyLinkedList<>();
            list.addLast("Sept 20");
            list.addLast("Sept 22");
            list.addFirst("Sept 24");

            assertEquals(3, list.size());
            assertEquals("Sept 24", list.getFirst(), "addFirst puts the newest at the HEAD");
            assertEquals("Sept 20", list.get(1));
            assertEquals("Sept 22", list.getLast());
            assertEquals(List.of("Sept 24", "Sept 20", "Sept 22"), list.toList());
        }

        @Test
        void removeAndReverseRewireTheChain() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            list.add(3);

            assertTrue(list.remove(2));
            assertEquals(List.of(1, 3), list.toList());

            list.reverse();
            assertEquals(List.of(3, 1), list.toList());
            assertEquals(3, list.getFirst());
            assertEquals(1, list.getLast());
            assertTrue(list.contains(3));
            assertFalse(list.contains(99));
        }

        @Test
        void insertAtPositionKeepsTheChainIntact() {
            MyLinkedList<String> list = new MyLinkedList<>();
            list.add("a");
            list.add("c");
            list.add(1, "b");
            list.add(3, "d");

            assertEquals(List.of("a", "b", "c", "d"), list.toList());
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(10));
        }
    }

    @Nested
    @DisplayName("MyStack (recent transactions / undo)")
    class StackTests {

        @Test
        void popsTheMostRecentFirst() {
            MyStack<Integer> stack = new MyStack<>();
            stack.push(102);
            stack.push(103);
            stack.push(104);

            assertEquals(104, stack.peek());
            assertEquals(104, stack.pop());
            assertEquals(103, stack.pop());
            assertEquals(1, stack.size());
        }

        @Test
        void toListIsTopFirst() {
            MyStack<String> stack = new MyStack<>();
            stack.push("oldest");
            stack.push("newest");

            assertEquals(List.of("newest", "oldest"), stack.toList());
            assertEquals(List.of("oldest", "newest"), stack.toListBottomFirst());
        }

        @Test
        void growsBeyondInitialCapacity() {
            MyStack<Integer> stack = new MyStack<>();
            for (int i = 0; i < 100; i++) {
                stack.push(i);
            }
            assertEquals(100, stack.size());
            assertEquals(99, stack.pop());
        }

        @Test
        void throwsWhenEmpty() {
            MyStack<Integer> stack = new MyStack<>();
            assertTrue(stack.isEmpty());
            assertNull(stack.peekOrNull());
            assertThrows(NoSuchElementException.class, stack::pop);
        }

        @Test
        void loadFromReplacesContents() {
            MyStack<String> stack = new MyStack<>();
            stack.push("stale");

            stack.loadFrom(List.of("a", "b", "c"));
            assertEquals(3, stack.size());
            assertEquals("c", stack.peek());
        }
    }
}
