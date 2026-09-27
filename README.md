# Barbershop Appointment & Queue Management System

Desktop barbershop management system built with **Java 21 + JavaFX + SQLite**, following an
**MVC-style** architecture. All Data Structures and Algorithms used by the system are
**custom implementations** written for this project (see `src/main/java/com/barbershop/dsa`).

## Requirements

- **JDK 21 or newer** — this is the only thing you need to install.
- Maven 3.9+ *or* the bundled Maven wrapper (recommended).
- JavaFX 21 and the SQLite JDBC driver are downloaded automatically by Maven.

Check your JDK with `java -version`. If you do not have one:

```bash
winget install --id Microsoft.OpenJDK.21 -e
```

## Running

```bash
./mvnw clean javafx:run      # Windows: mvnw.cmd clean javafx:run
```

The Maven wrapper downloads Maven itself on first use, so no separate Maven install is needed.
If you already have Maven, `mvn clean javafx:run` works identically.

The SQLite database (`barbershop.db`) is created automatically on first launch with an empty
schema and a single default login — no demo data. Add your barbers, services and customers
through the UI.

**Default login:** `admin` / `admin`

> When the app is started from the classpath, JavaFX logs a harmless
> `Unsupported JavaFX configuration: classes were loaded from 'unnamed module'` warning.

### Useful commands

| Command | Purpose |
|---|---|
| `./mvnw clean javafx:run` | Build and launch the application |
| `./mvnw clean test` | Run the tests (DSA, database and FXML smoke tests) |
| `./mvnw clean package` | Compile and produce the jar in `target/` |

### Opening in an IDE

Import the folder as a Maven project (IntelliJ, Eclipse or VS Code with the Java pack).
The main class is `com.barbershop.Launcher`.

## Project layout

```
src/main/java/com/barbershop/
├── Launcher.java              # plain entry point (avoids JavaFX module-path issues)
├── Main.java                  # JavaFX Application: boots database, shows login
├── model/                     # Customer, Barber, Service, Appointment, Transaction, QueueTicket, User
├── dsa/                       # custom data structures and algorithms
├── database/                  # DatabaseConnection, DatabaseInitializer and DAOs
├── controller/                # one controller per screen
└── util/                      # Session, Money, Dialogs, Validator

src/main/resources/
├── fxml/                      # one FXML file per screen
└── css/style.css              # JavaFX stylesheet
```

## Where the DSA is actually used

| DSA | Implementation | Real use in the system |
|---|---|---|
| **Queue** (FIFO) | `dsa/MyQueue.java` | Walk-in queue on the **Queue** screen: `enqueue` on add walk-in, `dequeue` on *Serve Next*. |
| **Priority Queue** (binary min-heap) | `dsa/MyPriorityQueue.java` | *Next up* panel — surfaces the earliest upcoming appointment for the day. |
| **Sorting** (merge sort) | `dsa/SortAlgorithms.java` | Appointments table sorting by time / customer / barber / service / price. |
| **Searching** (binary + linear) | `dsa/SearchAlgorithms.java` | Customer lookup: exact match via binary search over name-sorted data, partial match via linear scan. |
| **Linked List** (singly linked) | `dsa/MyLinkedList.java` | Customer service history chain ("View History"). |
| **Stack** (LIFO) | `dsa/MyStack.java` | Recently completed transactions and *Undo last payment*. |

## Architecture

```
JavaFX (FXML views)
        ↓
Controllers
        ↓
Business logic  ->  custom DSA (queue, priority queue, linked list, stack, search, sort)
        ↓
SQLite (JDBC via DAOs)
```
