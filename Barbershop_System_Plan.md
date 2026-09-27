# Barbershop Appointment & Queue Management System

## 1. Project Overview

A desktop **Barbershop Appointment & Queue Management System** built with:

- **Java**
- **JavaFX** for the GUI
- **SQLite** for the database
- **MVC-style architecture**
- Custom **Data Structures and Algorithms (DSA)** implementations

The main goal is to create a practical barbershop system while making Data Structures and Algorithms an important part of the actual system functionality rather than adding them artificially.

---

## 2. Main System Workflow

```text
Customer
   ↓
Choose service
   ↓
Choose barber
   ↓
Choose date/time
   ↓
Book appointment
   ↓
Appointment / Walk-in Queue
   ↓
Barber serves customer
   ↓
Payment
   ↓
Transaction History
```

---

## 3. Main Features

### Core Features

- Customer registration and management
- Barber management
- Service management
- Appointment booking
- Walk-in customer queue
- Queue management
- Transaction/payment recording
- Appointment history
- Customer search
- Appointment sorting
- Daily revenue overview
- Barber schedules
- Dashboard

### Possible Future Features

- Revenue reports and charts
- Appointment reminders
- Multiple barber scheduling
- Appointment priority handling
- Undo functionality
- More advanced reports

---

# 4. GUI Framework: JavaFX

## Why JavaFX?

JavaFX is the planned GUI framework because it provides:

- Modern-looking interfaces
- CSS styling
- Better tables and forms
- Scene Builder support
- Easier dashboard design
- A cleaner separation between UI and application logic

### Proposed Main Layout

```text
┌──────────────────────────────────────────────┐
│ BARBERSHOP MANAGEMENT SYSTEM                 │
├──────────────┬───────────────────────────────┤
│ Dashboard    │                               │
│ Appointments │          Dashboard            │
│ Customers    │                               │
│ Barbers      │   Today's Appointments: 12   │
│ Services     │   Customers Served: 8        │
│ Queue        │   Waiting Customers: 3       │
│ Transactions │   Today's Revenue: ₱2,450    │
└──────────────┴───────────────────────────────┘
```

---

# 5. Data Structures and Algorithms Requirement

The system will deliberately use several DSA concepts in real system operations.

## DSA #1 — Queue

### Purpose

Used for the barbershop's walk-in customers.

A queue follows **FIFO (First In, First Out)**.

Example:

```text
FRONT
  ↓
Juan → Pedro → Maria
                  ↑
                 REAR
```

Operations:

```java
enqueue(Customer)
dequeue()
peek()
isEmpty()
```

Example workflow:

```java
queue.enqueue(Juan);
queue.enqueue(Pedro);
queue.enqueue(Maria);

queue.dequeue();
```

The first customer added is the first customer served.

A custom queue implementation should be considered instead of relying entirely on Java's built-in queue classes so that the DSA implementation is visible in the project.

---

## DSA #2 — Priority Queue

### Purpose

Can be used to organize appointments according to priority, such as appointment time or a defined service priority.

Example:

```text
Priority Queue

┌─────────────────────┐
│ Appointment 10:00   │ ← highest priority
├─────────────────────┤
│ Appointment 10:15   │
├─────────────────────┤
│ Appointment 10:30   │
└─────────────────────┘
```

This is optional for the first implementation but can be added when the core system is stable.

---

## DSA #3 — Sorting

### Purpose

Appointments should be sortable by:

- Appointment time
- Customer name
- Barber
- Service
- Price

A sorting algorithm such as **Merge Sort** can be implemented.

Example:

```text
[10:30, 9:00, 11:00, 8:30]

        ↓
    Merge Sort
        ↓

[8:30, 9:00, 10:30, 11:00]
```

The sorting feature can be exposed through the appointments table in the GUI.

---

## DSA #4 — Searching

### Purpose

Used for finding customers and other records.

Possible algorithms:

- Linear Search
- Binary Search

Example:

```text
Customer list:

[Alex, Bob, Carlo, Daniel, Mark, Pedro, Sarah]

               ↓
          Binary Search
               ↓
             Pedro
```

The search algorithm can be connected to the customer search field.

---

## DSA #5 — Linked List

### Purpose

Can be used to maintain customer appointment/service history.

Example:

```text
HEAD
 ↓
[Haircut — Sept 20]
        ↓
[Haircut + Beard — Sept 22]
        ↓
[Haircut — Sept 24]
        ↓
NULL
```

Possible implementation:

```java
class Node {
    Appointment data;
    Node next;
}
```

This gives the project a clear linked-list use case instead of using a linked list only for demonstration.

---

## DSA #6 — Stack

### Purpose

Can be used for:

- Recently completed transactions
- Undo functionality
- Reverting the most recent action

Example:

```text
TOP
 ↓
Transaction #105
Transaction #104
Transaction #103
Transaction #102
```

A stack follows **LIFO (Last In, First Out)**.

This is a secondary DSA feature and does not need to be part of the earliest version.

---

# 6. Database

## Database Choice

Use **SQLite**.

Reasons:

- Lightweight
- No separate database server required
- Easy to bundle with a desktop Java application
- Suitable for a small academic project

## Tables

### `customers`

```text
customer_id
name
phone
email
```

### `barbers`

```text
barber_id
name
specialization
status
```

### `services`

```text
service_id
name
price
duration
```

### `appointments`

```text
appointment_id
customer_id
barber_id
service_id
appointment_date
appointment_time
status
```

### `transactions`

```text
transaction_id
appointment_id
amount
payment_method
transaction_date
```

---

# 7. Proposed Project Architecture

The project should separate the GUI, business logic, DSA, and database layers.

```text
                   ┌─────────────┐
                   │   JavaFX    │
                   │     GUI     │
                   └──────┬──────┘
                          ↓
                   ┌─────────────┐
                   │ Controllers │
                   └──────┬──────┘
                          ↓
             ┌────────────────────────┐
             │   Business Logic       │
             │                        │
             │ Queue                  │
             │ Priority Queue         │
             │ Linked List            │
             │ Stack                  │
             │ Search Algorithms      │
             │ Sorting Algorithms     │
             └───────────┬────────────┘
                         ↓
                  ┌─────────────┐
                  │   SQLite    │
                  └─────────────┘
```

---

# 8. Proposed Java Project Structure

```text
src/
│
├── model/
│   ├── Customer.java
│   ├── Barber.java
│   ├── Service.java
│   ├── Appointment.java
│   └── Transaction.java
│
├── dsa/
│   ├── CustomerQueue.java
│   ├── PriorityQueue.java
│   ├── LinkedList.java
│   ├── Stack.java
│   ├── SearchAlgorithms.java
│   └── SortAlgorithms.java
│
├── database/
│   ├── DatabaseConnection.java
│   ├── CustomerDAO.java
│   ├── AppointmentDAO.java
│   └── TransactionDAO.java
│
├── controller/
│   ├── DashboardController.java
│   ├── AppointmentController.java
│   ├── CustomerController.java
│   ├── QueueController.java
│   └── TransactionController.java
│
└── Main.java
```

---

# 9. Proposed GUI Screens

## Dashboard

Display:

```text
Today's Appointments: 12
Customers Served: 8
Waiting Customers: 3
Today's Revenue: ₱2,450
```

---

## Appointments

Example table:

```text
┌────────┬──────────┬────────┬────────┬────────┐
│ Time   │ Customer │ Barber │ Service│ Status │
├────────┼──────────┼────────┼────────┼────────┤
│ 10:00  │ Juan     │ Mark   │ Haircut│ Done   │
│ 10:30  │ Pedro    │ John   │ Beard  │ Waiting│
│ 11:00  │ Maria    │ Mark   │ Haircut│ Booked │
└────────┴──────────┴────────┴────────┴────────┘
```

Functions:

- Add appointment
- Edit appointment
- Cancel appointment
- Sort appointments
- Search/filter appointments

---

## Queue

This should be the most visible DSA feature.

```text
CURRENT CUSTOMER

Juan Dela Cruz
Haircut
Barber: Mark

[ SERVE ]

WAITING QUEUE

1. Pedro Santos
2. Maria Cruz
3. John Reyes
```

Functions:

- Add walk-in
- View current customer
- Serve next customer
- Remove/cancel customer
- View waiting queue

---

## Customers

Example:

```text
[ Search customer... ]

Juan Dela Cruz
0917-xxx-xxxx
Appointments: 14
Total Spent: ₱3,850

[ View History ]
```

Functions:

- Add customer
- Edit customer
- Delete customer
- Search customer
- View service/appointment history

---

## Services

Example:

```text
Haircut              ₱150
Haircut + Beard      ₱250
Beard Trim           ₱100
Hair Wash             ₱80
```

Functions:

- Add service
- Edit service
- Delete service
- Change price
- Set estimated duration

---

## Transactions

Example:

```text
Date        Customer        Service         Amount
Sep 24      Juan            Haircut         ₱150
Sep 24      Pedro           Beard           ₱100
Sep 24      Maria           Haircut         ₱150
```

---

# 10. Example of DSA Working Together

A typical scenario:

### Step 1 — Customers arrive

```text
Juan
Pedro
Maria
```

### Step 2 — Customers enter the queue

```java
queue.enqueue(Juan);
queue.enqueue(Pedro);
queue.enqueue(Maria);
```

Queue state:

```text
FRONT
 ↓
Juan → Pedro → Maria
```

### Step 3 — Barber serves the next customer

```java
queue.dequeue();
```

Queue becomes:

```text
FRONT
 ↓
Pedro → Maria
```

### Step 4 — Appointments are sorted

Original:

```text
[11:00, 9:00, 10:30, 8:30]
```

After Merge Sort:

```text
[8:30, 9:00, 10:30, 11:00]
```

### Step 5 — Customer search

```java
BinarySearch("Pedro");
```

The matching customer record is returned.

### Step 6 — Completed transaction is stored

```text
TOP
 ↓
Transaction 104
Transaction 103
Transaction 102
```

This demonstrates multiple DSA concepts working together in an actual application workflow.

---

# 11. Recommended Core Scope

To keep the project manageable, the first complete version should focus on:

### Required Core Features

- Login
- Dashboard
- Customer management
- Barber management
- Service management
- Appointment booking
- Walk-in queue
- Transaction recording
- Customer search
- Appointment sorting
- SQLite database

### Required DSA

- Queue
- Linked List
- Stack
- Sorting algorithm
- Searching algorithm

### Optional / Later Features

- Priority Queue
- Revenue charts
- Appointment reminders
- Advanced reports
- Undo operations
- More advanced barber scheduling

The project should be completed in stages rather than attempting every feature simultaneously.

---

# 12. Recommended Technology Stack

| Component | Technology |
|---|---|
| Programming Language | Java |
| GUI | JavaFX |
| UI Styling | JavaFX CSS |
| UI Designer | Scene Builder (optional) |
| Database | SQLite |
| Database Access | JDBC |
| Architecture | MVC-style |
| DSA | Custom Java implementations |
| Build Tool | Maven or Gradle |

---

# 13. Development Order

The project should be developed in the following order:

1. Set up JavaFX project
2. Create basic application window/navigation
3. Create model classes
4. Set up SQLite database and JDBC connection
5. Implement customer management
6. Implement barber management
7. Implement services
8. Implement appointments
9. Implement custom queue
10. Connect queue to JavaFX UI
11. Implement searching
12. Implement sorting
13. Implement linked-list history
14. Implement stack/undo or transaction history
15. Implement transaction/payment system
16. Build dashboard
17. Add validation and error handling
18. Improve UI styling
19. Test all features
20. Prepare DSA documentation and presentation

---

# 14. DSA Demonstration Goals

For the final project presentation or defense, be prepared to explain:

### Queue

- What FIFO means
- Why a queue fits walk-in customers
- Enqueue and dequeue operations
- Time complexity

### Linked List

- Node structure
- How nodes connect
- Why it is used for history
- Insertion/traversal operations

### Stack

- What LIFO means
- Why it fits undo/recent-history operations
- Push and pop operations

### Searching

- How the selected search algorithm works
- Why it is used for customer lookup
- Time complexity

### Sorting

- How the selected sorting algorithm works
- What information is sorted
- Time complexity

---

# 15. Overall Project Goal

The final application should feel like a small but complete barbershop management system, while clearly demonstrating that the required Data Structures and Algorithms are being used to solve real problems.

The guiding principle is:

> **The DSA should support the system's functionality, not exist only for compliance.**

Planned stack:

**Java + JavaFX + SQLite + MVC-style architecture + custom DSA implementations**
