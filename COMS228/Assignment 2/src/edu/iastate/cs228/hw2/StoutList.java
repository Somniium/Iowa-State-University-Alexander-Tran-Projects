package edu.iastate.cs228.hw2;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Implementation of the list interface based on linked nodes that store
 * multiple items per node. Rules for adding and removing elements ensure that
 * each node (except possibly the last one) is at least half full.
 * 
 * @author Alexander Tran
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E> {
	/**
	 * Default number of elements that may be stored in each node.
	 */
	private static final int DEFAULT_NODESIZE = 4;

	/**
	 * Number of elements that can be stored in each node.
	 */
	private final int nodeSize;

	/**
	 * Dummy node for head. It should be private but set to public here only for
	 * grading purpose. In practice, you should always make the head of a linked
	 * list a private instance variable.
	 */
	public Node head;

	/**
	 * Dummy node for tail.
	 */
	private Node tail;

	/**
	 * Number of elements in the list.
	 */
	private int size;

	/**
	 * Constructs an empty list with the default node size.
	 */
	public StoutList() {
		this(DEFAULT_NODESIZE);
	}

	/**
	 * Constructs an empty list with the given node size.
	 * 
	 * @param nodeSize number of elements that may be stored in each node, must be
	 *                 an even number
	 */
	public StoutList(int nodeSize) {
		if (nodeSize <= 0 || nodeSize % 2 != 0)
			throw new IllegalArgumentException();

		// dummy nodes
		head = new Node();
		tail = new Node();
		head.next = tail;
		tail.previous = head;
		this.nodeSize = nodeSize;
	}

	/**
	 * Constructor for grading only. Fully implemented.
	 * 
	 * @param head
	 * @param tail
	 * @param nodeSize
	 * @param size
	 */
	public StoutList(Node head, Node tail, int nodeSize, int size) {
		this.head = head;
		this.tail = tail;
		this.nodeSize = nodeSize;
		this.size = size;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean add(E item) {
		if (item == null) {
			throw new NullPointerException();
		}

		add(size, item);
		return true;
	}

	/*
	 * 
	 */
	private class NodeInfo {
		public Node node;
		public int offset;

		public NodeInfo(Node node, int offset) {
			this.node = node;
			this.offset = offset;
		}
	}

	/*
	 * 
	 */
	private NodeInfo find(int pos) {
		if (pos < 0 || pos > size) {
			throw new IndexOutOfBoundsException();
		}
		if (pos == size) {
			return new NodeInfo(tail, 0);
		}
		int total = 0;
		Node current = head.next;
		while (current != tail) {
			if (pos < total + current.count) {
				int offset = pos - total;
				return new NodeInfo(current, offset);
			}
			total += current.count;
			current = current.next;
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public void add(int pos, E item) {
		if (item == null) {
			throw new NullPointerException();
		}
		if (pos < 0 || pos > size) {
			throw new IndexOutOfBoundsException();
		}

		if (head.next == tail) {
			Node n = new Node();
			n.previous = head;
			n.next = tail;
			head.next = n;
			tail.previous = n;
			n.addItem(item);
			size++;
			return;
		}
		NodeInfo nInfo = find(pos);
		Node node = nInfo.node;
		int offset = nInfo.offset;

		if (offset == 0) {
			Node prevN = node.previous;
			if (prevN != head && prevN.count < nodeSize) {
				prevN.addItem(item);
				size++;
				return;
			}
			if (node == tail) {
				Node n = new Node();
				n.previous = prevN;
				prevN.next = n;
				tail.previous = n;
				n.next = tail;
				n.addItem(item);
				size++;
				return;
			}
		}
		if (node.count < nodeSize) {
			node.addItem(offset, item);

			size++;
			return;
		}
		Node newNode = new Node();
		node.next.previous = newNode;
		newNode.next = node.next;
		node.next = newNode;
		newNode.previous = node;

		int numMove = nodeSize / 2;
		for (int i = 0; i < numMove; i++) {
			newNode.addItem(0, node.data[node.count - 1]);
			node.removeItem(node.count - 1);
		}

		if (offset <= node.count) {
			node.addItem(offset, item);
		} else {
			newNode.addItem(offset - node.count, item);
		}
		size++;
	}

	@Override
	public E remove(int pos) {
	    if (pos < 0 || pos >= size) {
	        throw new IndexOutOfBoundsException();
	    }

	    NodeInfo nInfo = find(pos);
	    Node node = nInfo.node;
	    int offset = nInfo.offset;
	    E removedItem = node.data[offset];

	    node.removeItem(offset);
	    size--;

	    if (node.count == 0) {
	        // Remove empty node
	        node.previous.next = node.next;
	        node.next.previous = node.previous;
	    } else if (node.count < nodeSize / 2 && node.next != tail) {
	        // Modified condition: allow first node to merge if underfull
	        Node nextNode = node.next;
	        if (nextNode.count > nodeSize / 2) {
	            // Mini-merge: move first element from nextNode to node
	            node.addItem(nextNode.data[0]);
	            nextNode.removeItem(0);
	        } else {
	            // Full merge: move all elements from nextNode to node
	            for (int i = 0; i < nextNode.count; i++) {
	                node.addItem(nextNode.data[i]);
	            }
	            node.next = nextNode.next;
	            nextNode.next.previous = node;
	        }
	    }
	    return removedItem;
	}

	/**
	 * Sort all elements in the stout list in the NON-DECREASING order. You may do
	 * the following. Traverse the list and copy its elements into an array,
	 * deleting every visited node along the way. Then, sort the array by calling
	 * the insertionSort() method. (Note that sorting efficiency is not a concern
	 * for this project.) Finally, copy all elements from the array back to the
	 * stout list, creating new nodes for storage. After sorting, all nodes but
	 * (possibly) the last one must be full of elements.
	 * 
	 * Comparator<E> must have been implemented for calling insertionSort().
	 */
	public void sort() {
		// Traverse the list and copy its elements into an array, deleting every visited
		// node along the way.
		E[] arr = (E[]) new Comparable[size];
		int i = 0;
		Node current = head.next;
		while (current != tail) {
			for (int j = 0; j < current.count; j++) {
				arr[i++] = current.data[j];
			}
			Node nextNode = current.next;
			current.previous.next = nextNode;
			nextNode.previous = current.previous;
			current.count = 0; // Clear the node
			current = nextNode;
		}

		head.next = tail;
		tail.previous = head;
		size = 0;

		// Sort the array.
		this.insertionSort(arr, new Comparator<E>() {
			@Override
			public int compare(E o1, E o2) {
				return o1.compareTo(o2);
			}
		});

		// Create new nodes in the list and add elements back from the sorted array.
		for (E item : arr) {
			add(item);
		}
	}

	/**
	 * Sort all elements in the stout list in the NON-INCREASING order. Call the
	 * bubbleSort() method. After sorting, all but (possibly) the last nodes must be
	 * filled with elements.
	 * 
	 * Comparable<? super E> must be implemented for calling bubbleSort().
	 */
	public void sortReverse() {
		// Traverse the list and copy its elements into an array, deleting every visited
		// node along the way.
		E[] arr = (E[]) new Comparable[size];
		int i = 0;
		Node current = head.next;
		while (current != tail) {
			for (int j = 0; j < current.count; j++) {
				arr[i++] = current.data[j];
			}
			Node nextNode = current.next;
			current.previous.next = nextNode;
			nextNode.previous = current.previous;
			current.count = 0; // Clear the node
			current = nextNode;
		}

		head.next = tail;
		tail.previous = head;
		size = 0;

		// Sort the array.
		this.bubbleSort(arr);

		// Same as above sorting method.
		for (E item : arr) {
			add(item);
		}
	}

	@Override
	public Iterator<E> iterator() {
		return new StoutListIterator();
	}

	@Override
	public ListIterator<E> listIterator() {
		return new StoutListIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new StoutListIterator(index);
	}

	/**
	 * Returns a string representation of this list showing the internal structure
	 * of the nodes.
	 */
	public String toStringInternal() {
		return toStringInternal(null);
	}

	/**
	 * Returns a string representation of this list showing the internal structure
	 * of the nodes and the position of the iterator.
	 *
	 * @param iter an iterator for this list
	 */
	public String toStringInternal(ListIterator<E> iter) {
		int count = 0;
		int position = -1;
		if (iter != null) {
			position = iter.nextIndex();
		}

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		Node current = head.next;
		while (current != tail) {
			sb.append('(');
			E data = current.data[0];
			if (data == null) {
				sb.append("-");
			} else {
				if (position == count) {
					sb.append("| ");
					position = -1;
				}
				sb.append(data.toString());
				++count;
			}

			for (int i = 1; i < nodeSize; ++i) {
				sb.append(", ");
				data = current.data[i];
				if (data == null) {
					sb.append("-");
				} else {
					if (position == count) {
						sb.append("| ");
						position = -1;
					}
					sb.append(data.toString());
					++count;

					// iterator at end
					if (position == size && count == size) {
						sb.append(" |");
						position = -1;
					}
				}
			}
			sb.append(')');
			current = current.next;
			if (current != tail)
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Node type for this list. Each node holds a maximum of nodeSize elements in an
	 * array. Empty slots are null.
	 */
	private class Node {
		/**
		 * Array of actual data elements.
		 */
		public E[] data = (E[]) new Comparable[nodeSize];

		/**
		 * Link to next node.
		 */
		public Node next;

		/**
		 * Link to previous node;
		 */
		public Node previous;

		/**
		 * Index of the next available offset in this node, also equal to the number of
		 * elements in this node.
		 */
		public int count;

		/**
		 * Adds an item to this node at the first available offset. Precondition: count
		 * < nodeSize
		 * 
		 * @param item element to be added
		 */
		void addItem(E item) {
			if (count >= nodeSize) {
				return;
			}
			data[count++] = item;
		}

		/**
		 * Adds an item to this node at the indicated offset, shifting elements to the
		 * right as necessary.
		 * 
		 * Precondition: count < nodeSize
		 * 
		 * @param offset array index at which to put the new element
		 * @param item   element to be added
		 */
		void addItem(int offset, E item) {
			if (count >= nodeSize) {
				return;
			}
			for (int i = count - 1; i >= offset; --i) {
				data[i + 1] = data[i];
			}
			count++;
			data[offset] = item;
		}

		/**
		 * Deletes an element from this node at the indicated offset, shifting elements
		 * left as necessary. Precondition: 0 <= offset < count
		 * 
		 * @param offset
		 */
		void removeItem(int offset) {
			E item = data[offset];
			for (int i = offset + 1; i < nodeSize; ++i) {
				data[i - 1] = data[i];
			}
			data[count - 1] = null;
			count--;
		}
	}

	private class StoutListIterator implements ListIterator<E> {
		// constants you possibly use ...

		// instance variables ...
		private Node currentNode;
		private int offset;
		private int index;
		private boolean canRemoveOrSet;
		private boolean lastWasNext;

		/**
		 * Default constructor
		 */
		public StoutListIterator() {
			this(0);
		}

		/**
		 * Constructor finds node at a given position.
		 * 
		 * @param pos
		 */
		public StoutListIterator(int pos) {
			if (pos < 0 || pos > size) {
				throw new IndexOutOfBoundsException();
			}
			NodeInfo info = find(pos);
			currentNode = info.node;
			offset = info.offset;
			index = pos;
			canRemoveOrSet = false;
			lastWasNext = false;
		}

		@Override
		public boolean hasNext() {
			return index < size;
		}

		@Override
		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			while (currentNode != tail && (currentNode.count == 0 || offset >= currentNode.count)) {
				currentNode = currentNode.next;
				offset = 0;
			}

			if (currentNode == tail) {
				throw new NoSuchElementException();
			}

			E item = currentNode.data[offset];
			offset++;
			index++;
			canRemoveOrSet = true;
			lastWasNext = true;
			return item;
		}

		@Override
		public void remove() {
		    if (!canRemoveOrSet) {
		        throw new IllegalStateException();
		    }
		    
		    int removeIndex;
		    if (lastWasNext) { 
		        removeIndex = index - 1;
		    } else { 
		        removeIndex = index;
		    }
		    
		    StoutList.this.remove(removeIndex);
		    
		    if (lastWasNext) { 
		        index--;
		    }
		    
		    // Update current node and offset
		    if (index < size) {
		        NodeInfo info = find(index);
		        currentNode = info.node;
		        offset = info.offset;
		    } else {
		        currentNode = tail;
		        offset = 0;
		    }
		    
		    canRemoveOrSet = false;
		}

		// Other methods you may want to add or override that could possibly facilitate
		// other operations, for instance, addition, access to the previous element,
		// etc.
		//
		// ...
		//
		@Override
		public boolean hasPrevious() {
			return index > 0;
		}

		@Override
		public E previous() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			if (offset == 0) {
				currentNode = currentNode.previous;
				offset = currentNode.count;
			}
			offset--;
			index--;
			canRemoveOrSet = true;
			lastWasNext = false;
			return currentNode.data[offset];
		}

		@Override
		public int nextIndex() {
			return index;
		}

		@Override
		public int previousIndex() {
			return index - 1;
		}

		@Override
		public void add(E e) {
			if (e == null) {
				throw new NullPointerException();
			}
			StoutList.this.add(index, e);
			index++;
			offset++;
			canRemoveOrSet = false;
		}

		@Override
		public void set(E e) {
			if (!canRemoveOrSet) {
				throw new IllegalStateException();
			}
			if (e == null) {
				throw new NullPointerException();
			}

			if (offset == 0) { 
				// Element was returned by previous()
				currentNode.data[offset] = e;
			} else { 
				// Element was returned by next()
				currentNode.data[offset - 1] = e;
			}
		}
	}

	/**
	 * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING
	 * order.
	 * 
	 * @param arr  array storing elements from the list
	 * @param comp comparator used in sorting
	 */
	private void insertionSort(E[] arr, Comparator<? super E> comp) {
		for (int i = 1; i < arr.length; i++) {
			E key = arr[i];
			int j = i - 1;
			while (j >= 0 && comp.compare(arr[j], key) > 0) {
				arr[j + 1] = arr[j];
				j--;
			}
			arr[j + 1] = key;
		}
	}

	/**
	 * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a
	 * description of bubble sort please refer to Section 6.1 in the project
	 * description. You must use the compareTo() method from an implementation of
	 * the Comparable interface by the class E or ? super E.
	 * 
	 * @param arr array holding elements from the list
	 */
	private void bubbleSort(E[] arr) {
		int n = arr.length;
		boolean swapped;
		for (int i = 0; i < n - 1; i++) {
			swapped = false;
			for (int j = 0; j < n - 1 - i; j++) {
				if (arr[j].compareTo(arr[j + 1]) < 0) { // For non-increasing order
					// swap arr[j] and arr[j+1]
					E temp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp;
					swapped = true;
				}
			}
			// If no two elements were swapped by inner loop, then break
			if (swapped == false)
				break;
		}
	}
}