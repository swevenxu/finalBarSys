# FadeIntoYou - Study Notes for the Defense

Everything here matches the actual code in this repo. File references are real,
so you can open them while practicing.

---

## 1. The Big Picture (say this first)

FadeIntoYou is a desktop barbershop appointment and queue management system.

- **Language/Framework:** Java 21, JavaFX (UI), SQLite (database via JDBC)
- **Architecture:** MVC-style layering
  - **View** = FXML files in `src/main/resources/fxml/` (one per screen)
  - **Controller** = `controller/` package (one per screen, handles buttons/tables)
  - **Model** = `model/` package (Customer, Barber, Service, Appointment, Transaction, QueueTicket, User)
  - **Database** = `database/` package (DAOs - Data Access Objects - one per table)
  - **DSA** = `dsa/` package (all hand-written, no java.util collections for the core structures)
- **Flow:** FXML screen -> Controller -> Service/DSA -> DAO -> SQLite

Key selling point for the panel: the program does NOT use Java's built-in
`LinkedList`, `PriorityQueue`, `Stack`, or `Collections.sort()` for its core
features. We wrote our own.

---

## 2. The Six Data Structures / Algorithms

### 2.1 Queue (FIFO) - `dsa/MyQueue.java`

**Concept:** First-In-First-Out. Like a real line of people.

**Implementation:** Singly linked list of `Node<E>` objects (each node holds
`data` + `next` pointer). Keeps TWO pointers: `front` and `rear`.

| Operation | What it does | Complexity |
|---|---|---|
| `enqueue(item)` | adds at the rear | O(1) |
| `dequeue()` | removes from the front | O(1) |
| `peek()` | looks at the front | O(1) |
| `remove(item)` | removes a specific customer (they left) | O(n) |

**Why two pointers matter (common question):** if we only had `front`,
enqueue would have to walk the whole list to find the end = O(n). Storing
`rear` makes enqueue O(1). The `rear.next` is always null.

**Where it's used:** the Walk-in Queue screen (`controller/QueueController.java`,
wrapped by `service/QueueService.java`).
- Add walk-in -> `enqueue()`
- Serve Next -> `dequeue()` (creates the appointment + payment)
- The screen even draws `FRONT -> ... -> REAR` using `toString()` to show the
  pointer chain.

---

### 2.2 Priority Queue (binary MIN-heap) - `dsa/MyPriorityQueue.java`

**Concept:** Not "first come first served" - the *highest priority* item comes
out first, no matter when it was added.

**Implementation:** an `ArrayList` used as a binary heap + a `Comparator<T>`
passed to the constructor (that's how "priority" is defined - for us, earliest
appointment time wins).

Array-to-tree mapping (memorize this):
- parent of node `i` is at `(i - 1) / 2`
- left child at `2i + 1`, right child at `2i + 2`

| Operation | How | Complexity |
|---|---|---|
| `insert(item)` | add at the end, then **sift up** (swap with parent while smaller) | O(log n) |
| `poll()` | take root, move last element to root, then **sift down** (swap with smaller child) | O(log n) |
| `peek()` | read index 0 | O(1) |

**Why log n:** the heap is a balanced tree stored in an array; sifting travels
at most the height of the tree, and height = log2(n).

**Where it's used:** the "Next up" panel on the Dashboard
(`controller/DashboardController.java`). All of today's BOOKED/WAITING
appointments are inserted into a `MyPriorityQueue` ordered by
`Appointment.BY_DATE_TIME`, and `peek()` shows the earliest one.

**Possible question: "min-heap or max-heap?"** Min-heap - the smallest
date/time (= earliest appointment) is at the root.

---

### 2.3 Linked List (singly) - `dsa/MyLinkedList.java`

**Concept:** a chain of nodes, each pointing to the next. Good for fast
insertion at the head and showing history "newest first".

**Implementation:** `Node<T>` with `data` + `next`. Keeps both `head` and
`tail` pointers, plus a `size` counter.

| Operation | Complexity | Notes |
|---|---|---|
| `addFirst` | O(1) | push history entry |
| `addLast` | O(1) | thanks to the tail pointer |
| `get(i)` / `remove(i)` | O(n) | must walk from the head |
| `contains` | O(n) | linear scan |

**Where it's used:** "View History" on the Customers screen
(`controller/CustomerController.java`). The customer's past appointments are
pulled newest-first from the DB and pushed into a `MyLinkedList` with
`addFirst`, then traversed with the custom `Iterator` to render the chain
(e.g. `Haircut -> Haircut + Beard -> null`).

**Possible question: "ArrayList vs LinkedList?"** ArrayList = O(1) random
access but O(n) insert at front (shifts elements). LinkedList = O(1)
insert/remove at known positions, O(n) access. We chose the list because
history is appended/inserted and read in order, not randomly accessed.

---

### 2.4 Stack (LIFO) - `dsa/MyStack.java`

**Concept:** Last-In-First-Out. Like a stack of plates - undo operations.

**Implementation:** a dynamic `Object[]` array (starts at capacity 16).
- `push` -> `elements[size++] = item`
- `pop` -> takes `elements[--size]`, then sets the slot to null (so the GC
  can collect it - nice detail to mention)
- `grow()` doubles capacity and copies with `System.arraycopy` when full
  (amortized O(1) per push - same idea as ArrayList)

| Operation | Complexity |
|---|---|
| `push` | O(1) amortized |
| `pop` / `peek` | O(1) |

**Where it's used:** the Transactions screen
(`controller/TransactionController.java`). Every recorded payment is pushed;
the "top of stack" card shows the most recent payment; **Undo last payment**
pops it and deletes it from the DB.

**Possible question: "why is a stack right for undo?"** Undo always reverses
the MOST RECENT action - exactly LIFO order.

---

### 2.5 Sorting (merge sort + insertion sort) - `dsa/SortAlgorithms.java`

**Merge sort** (the main one):
1. Split the list in half recursively until pieces of size 1
2. Merge pairs of sorted halves by comparing front elements

| Property | Value |
|---|---|
| Time | O(n log n) in ALL cases (best/average/worst) |
| Space | O(n) (it uses a `buffer` copy of the list) |
| Stable? | Yes - equal elements keep their relative order (the `<=` in the merge is what makes it stable - mention this!) |

**Insertion sort** is also included and used as the "simple" O(n^2)
counterpart for small data - good to contrast when the professor asks
"why not just insertion sort everything?"

**Where it's used:** the Appointments screen
(`controller/AppointmentController.java`). The "Sort by" combo lets the user
sort by time / customer / barber / service / price - each choice just swaps
the `Comparator` passed to `mergeSort`.

**Possible question: "how do you sort by different columns without rewriting
the sort?"** The algorithm is generic (`<T>`) and only calls
`comparator.compare(...)` - changing the sort key means passing a different
Comparator, not touching the algorithm.

---

### 2.6 Searching (linear + binary) - `dsa/SearchAlgorithms.java`

**Linear search** (`linearSearch` / `findFirst`): scan one by one with a
`Predicate` as the match condition. O(n). Works on ANY list, sorted or not.
Used for the partial-match / filter behavior.

**Binary search** (`binarySearch`): repeatedly halve a SORTED list -
compare the middle element, go left or right. O(log n).

| n | linear worst | binary worst |
|---|---|---|
| 1,000 | 1,000 checks | ~10 checks |
| 1,000,000 | 1,000,000 checks | ~20 checks |

**The catch to mention:** binary search REQUIRES sorted data. That's why the
Customers screen first sorts customer names with our merge sort, THEN binary
searches by exact name (`CustomerController.java`).

There's also `lowerBound` (first index where the element is >= key) - the
classic "leftmost insertion point" variant.

---

## 3. Database Design (SQLite)

Tables (see `database/DatabaseInitializer.java`):

- **users** (user_id, username, password_hash, full_name, role)
- **customers** (customer_id, name, phone, email)
- **barbers** (barber_id, name, specialization, status)
- **services** (service_id, name, price, duration)
- **appointments** (appointment_id, customer_id, barber_id, service_id, date, time, status)
- **transactions** (transaction_id, appointment_id, amount, payment_method, date)

Talking points:
- **Foreign keys + `PRAGMA foreign_keys = ON`** (issued on every connection in
  `DatabaseConnection.java`) - deleting a customer cascades to their
  appointments and payments.
- **DAO pattern**: each table has a DAO class that converts rows to model
  objects (e.g. `AppointmentDAO.map()` builds an `Appointment` from a
  `ResultSet`). SQL lives ONLY in the `database/` package - controllers never
  touch SQL.
- **PreparedStatements everywhere** - the answer to "how do you prevent SQL
  injection?"
- **Passwords are hashed** with SHA-256 + salt (`util/PasswordUtil.java`) -
  never stored as plain text. (Be honest it's not bcrypt if asked.)
- The 19-service menu is seeded on first launch (only when the table is empty).

---

## 4. JavaFX / MVC Talking Points

- FXML = XML describing the UI; each screen has `fx:controller="..."` binding
  it to a controller class. `@FXML` fields link to `fx:id` nodes; buttons use
  `onAction="#handlerName"`.
- `util/ViewLoader.java` centralizes loading FXML + wiring controllers, and
  exposes the controller so the app can call `refresh()` on screen switches
  (`controller/Refreshable.java` interface).
- `MainController` is the shell: sidebar menu swaps screens into a shared
  `contentArea` and calls `refresh()` so every screen reloads fresh data.
- Observable lists (`FXCollections.observableArrayList`) back the TableViews -
  UI updates automatically when the list changes.
- JavaFX UI must be modified on the **JavaFX Application Thread** -
  `Platform.runLater` if needed (mention if asked about threading).

---

## 5. Where EVERY DSA Meets EVERY Screen (quick demo map)

| Screen | DSA in action | What to click |
|---|---|---|
| Dashboard | Priority queue | "Next up" panel - earliest booking on top |
| Appointments | Merge sort + linear search | change "Sort by", type in the filter box |
| Walk-in Queue | Queue (FIFO) | add walk-in -> Serve Next -> front of line leaves first |
| Customers | Merge sort + binary search + linked list | search a name; "View History" chain |
| Services | (data) | shows the seeded 19-service menu |
| Transactions | Stack (LIFO) | record 2 payments, undo the last one |
| Login | SHA-256 hashing | admin / admin |

---

## 6. Numbers to Memorize

- Queue/Stack/LinkedList insert/remove at ends: **O(1)** (linked queue keeps
  front AND rear; stack uses dynamic array)
- Heap insert/poll: **O(log n)**; peek: **O(1)**
- Merge sort: **O(n log n)** always, **O(n)** extra space, **stable**
- Insertion sort: **O(n^2)** worst/average, **O(n)** if already sorted,
  in-place
- Linear search: **O(n)**; Binary search: **O(log n)** but needs sorted data

---

## 7. Likely Professor Questions (rehearse these)

1. **"Why write your own structures instead of java.util?"**
   Requirement of the project; also to demonstrate we understand the internals
   (pointers, sift up/down, merge, capacity doubling).
2. **"Why a queue for walk-ins?"** Real barbershop fairness: first come,
   first served = FIFO.
3. **"Why a priority queue for 'Next up' but a plain queue for walk-ins?"**
   Appointments have planned times (earliest matters); walk-ins are strictly
   arrival order.
4. **"Is your merge sort stable? Prove it."** Yes - in the merge step, ties
   take from the LEFT run first (`<=`).
5. **"Why is binary search faster?"** Halves the search space each step -
   log2(n) comparisons. But it needs sorted input, which we produce with our
   merge sort.
6. **"What happens when the stack array is full?"** `grow()` doubles the
   capacity and copies elements - amortized O(1) push.
7. **"Where could data be lost?"** The walk-in queue lives in memory for the
   session (by design - it's "today's line"). Persisted data (customers,
   appointments, payments) lives in SQLite.
8. **"How do you avoid SQL injection?"** PreparedStatements with `?`
   parameters everywhere.
9. **"Why SQLite?"** Zero setup, single file (`barbershop.db`), perfect for a
   desktop app; JDBC means we could swap to MySQL with minimal DAO changes.
10. **"What design patterns did you use?"** DAO (database access), Singleton
    (`QueueService.getInstance()`), MVC (FXML + controllers), Observer-ish
    (JavaFX observable lists).

---

## 8. Two-Minute Opening Script (customize in your voice)

> "FadeIntoYou is a barbershop management desktop app built with Java, JavaFX
> and SQLite, following MVC. The heart of the project is that every core
> feature runs on data structures we implemented ourselves. Walk-in customers
> are managed by a hand-written queue - a linked list with front and rear
> pointers, so enqueue and dequeue are both O(1). The dashboard's 'Next up'
> panel runs on a hand-written binary min-heap, so the earliest appointment is
> found in O(1) after O(log n) inserts. Appointment tables are sorted by our
> own merge sort - O(n log n) and stable - and customer lookup combines that
> sorting with our own binary search for O(log n) exact matching. Payment
> history uses our own stack, built on a capacity-doubling array, to power
> 'undo last payment'. All of this is persisted through DAO classes using
> PreparedStatements against SQLite, with hashed passwords and foreign-key
> cascades. In short: the shop runs on our own algorithms end to end."
