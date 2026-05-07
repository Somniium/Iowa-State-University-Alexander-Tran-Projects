# StoutList

A Java linked data structure project created for COM S 228.  
This project implements a custom list called `StoutList`, a doubly linked list where each node stores multiple elements instead of only one.

## Project Overview

`StoutList` is a custom implementation of a list data structure. It extends Java's `AbstractSequentialList` and supports standard `List` and `ListIterator` behavior while using a more complex internal layout than a normal linked list.

Instead of storing one element per node, each node stores an array of up to `M` elements. This makes the structure a hybrid between an array-based list and a linked list. The list uses dummy head and tail nodes, and the actual data is stored in the nodes between them.

The project focuses on linked data structures, iterators, list operations, node splitting, node merging, sorting, and debugging internal structure.

## Features

- Implements a custom generic list structure
- Extends `AbstractSequentialList`
- Uses a doubly linked list with dummy head and tail nodes
- Stores multiple elements inside each node
- Does not allow `null` elements
- Supports index-based add and remove operations
- Supports forward iteration
- Supports full `ListIterator` behavior
- Maintains node capacity rules after add and remove operations
- Splits nodes when they become full
- Merges or redistributes nodes after removals
- Provides internal string output for debugging
- Supports sorting in ascending order
- Supports sorting in descending order

## Technologies Used

- Java
- Generic classes
- Linked data structures
- Arrays
- Iterators
- `ListIterator`
- `AbstractSequentialList`
- Comparators
- Insertion sort
- Bubble sort
- Javadoc

## Main Class

### `StoutList<E>`

The `StoutList` class stores elements in a linked chain of nodes. Each node contains an array of elements with a fixed capacity.

Main responsibilities:

- Track the total number of elements
- Add elements to the end of the list
- Add elements at a specific logical index
- Remove elements from a specific logical index
- Find the node and offset for a logical index
- Return iterators and list iterators
- Sort elements
- Preserve the required internal node structure

## Inner Classes

### `Node`

Represents one storage node in the list.

Each node contains:

- An array of elements
- A count of how many elements are currently stored
- A reference to the previous node
- A reference to the next node

The node also provides helper behavior for inserting and removing elements within its internal array.

### `StoutIterator`

Implements basic forward iteration through the list.

Main responsibilities:

- Check whether another element exists
- Return the next element
- Move across node boundaries correctly

### `StoutListIterator`

Implements the full `ListIterator<E>` interface.

Main responsibilities:

- Move forward and backward through the list
- Track the logical cursor position
- Support `next()` and `previous()`
- Support `nextIndex()` and `previousIndex()`
- Support `set()`
- Support iterator-based `add()`
- Support iterator-based `remove()`

## Internal Structure

Unlike a normal linked list, `StoutList` stores several elements in each node.

For example, with node capacity `M = 4`, a list may internally look like this:

```text
[(A, B, -, -), (C, D, E, -)]
```

The `-` symbols represent empty slots inside a node.

Logical indices are based on the full list order, not the node number. For example:

```text
Index 0 -> A
Index 1 -> B
Index 2 -> C
Index 3 -> D
Index 4 -> E
```

To find an element by index, the list must locate both:

1. The node containing the element
2. The offset inside that node's array

## Add Rules

When adding an element, the list follows special rules to keep nodes balanced:

- If the list is empty, create a new node.
- If adding at the beginning of a node and the previous node has space, place the element in the previous node.
- If adding at the end and the last node is full, create a new node.
- If the target node has space, insert the element and shift values as needed.
- If the target node is full, split the node and insert the new element into the correct half.

## Remove Rules

When removing an element, the list also follows balancing rules:

- If the target node is the last node and only has one element, delete the node.
- If the target node has enough elements, remove the element and shift values.
- If the node becomes underfilled, use the successor node to rebalance.
- If the successor has extra elements, perform a mini-merge by moving one element over.
- If the successor does not have enough extra elements, perform a full merge and delete the successor node.

## Sorting

The project includes two sorting methods.

### `sort()`

Sorts the list in non-decreasing order.

This method:

1. Copies the list elements into an array.
2. Clears the current node structure.
3. Sorts the array using insertion sort.
4. Rebuilds the list with nodes filled as much as possible.

### `sortReverse()`

Sorts the list in non-increasing order.

This method:

1. Copies the list elements into an array.
2. Clears the current node structure.
3. Sorts the array using bubble sort.
4. Rebuilds the list with nodes filled as much as possible.

## Example Usage

```java
StoutList<String> list = new StoutList<>();

list.add("A");
list.add("B");
list.add("C");
list.add("D");

list.add(2, "X");

System.out.println(list);
System.out.println(list.toStringInternal());

list.remove(1);

System.out.println(list.toStringInternal());
```

## What I Learned

Through this project, I gained a much deeper understanding of linked data structures. A regular linked list is already pointer-heavy, but this project made the design more challenging because each node also contained an internal array of elements.

I also practiced implementing Java's `List` and `ListIterator` behavior more closely. This helped me understand how cursor positions, logical indices, `next()`, `previous()`, `add()`, `remove()`, and `set()` are expected to work.

## Challenges

The hardest part of this project was keeping the logical list order consistent while the internal node structure changed. Adding or removing one element could require shifting values inside a node, splitting a full node, borrowing from a successor node, or merging two nodes together.

The iterator methods were also challenging because the cursor position had to stay correct even when elements were added or removed through the iterator.

## How to Run

1. Place the source file in the package structure:

```text
edu/iastate/cs228/hw2/StoutList.java
```

2. Compile the project in a Java IDE or from the command line.

3. Create a separate test class with a `main` method.

4. Instantiate a `StoutList` and test add, remove, iterator, and sorting behavior.

Example:

```java
public class StoutListTest {
    public static void main(String[] args) {
        StoutList<Integer> list = new StoutList<>();

        list.add(5);
        list.add(2);
        list.add(9);
        list.add(1);

        System.out.println("Before sort:");
        System.out.println(list.toStringInternal());

        list.sort();

        System.out.println("After sort:");
        System.out.println(list.toStringInternal());

        list.sortReverse();

        System.out.println("After reverse sort:");
        System.out.println(list.toStringInternal());
    }
}
```

## Author

Alexander Tran
