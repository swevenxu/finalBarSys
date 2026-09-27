# FadeIntoYou

A barbershop appointment and queue management desktop app. Built with Java 21,
JavaFX and SQLite. Made for a school project - the data structures and algorithms
(queue, stack, linked list, priority queue, sorting, searching) are all written
by hand, no java.util collections for those parts.

## How to run

You need JDK 21. Then:

```
mvnw.cmd clean javafx:run
```

On Mac/Linux use `./mvnw clean javafx:run` instead.

First run creates `barbershop.db` automatically.

Default login: **admin / admin**

## Features

- Login screen
- Dashboard (today's appointments, revenue, next customer)
- Appointments - book, update, cancel, mark as done
- Customers - add/edit/delete, view service history
- Barbers and Services management
- Walk-in queue (add, serve next, cancel)
- Transactions - record payment, undo last payment

## Where the DSA is used

| Data Structure | File | Used for |
|---|---|---|
| Queue | `dsa/MyQueue.java` | walk-in queue |
| Priority Queue | `dsa/MyPriorityQueue.java` | "next up" on dashboard |
| Linked List | `dsa/MyLinkedList.java` | customer service history |
| Stack | `dsa/MyStack.java` | undo last payment |
| Merge/Insertion Sort | `dsa/SortAlgorithms.java` | sorting tables |
| Binary/Linear Search | `dsa/SearchAlgorithms.java` | customer search |

## Tests

```
mvnw.cmd clean test
```
