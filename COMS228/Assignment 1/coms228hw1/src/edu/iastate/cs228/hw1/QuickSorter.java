package edu.iastate.cs228.hw1;

import java.io.FileNotFoundException;
import java.lang.NumberFormatException;
import java.lang.IllegalArgumentException;
import java.util.Comparator;
import java.util.InputMismatchException;

/**
 *  
 * @author Alexander Tran
 *
 */

/**
 * 
 * This class implements the version of the quicksort algorithm presented in the
 * lecture.
 *
 */

public class QuickSorter extends AbstractSorter {

	// Other private instance variables if you need ...

	/**
	 * Constructor takes an array of points. It invokes the superclass constructor,
	 * and also set the instance variables algorithm in the superclass.
	 * 
	 * @param pts input array of integers
	 */
	public QuickSorter(Point[] pts) {
		super(pts);
		algorithm = "Quick Sort";
	}

	/**
	 * Carry out quicksort on the array points[] of the AbstractSorter class.
	 * 
	 */
	@Override
	public void sort() {
		// Initialize comparator if it's null (for JUnit tests that set Point.xORy
		// directly)
		if (this.pointComparator == null) {
			this.pointComparator = (Comparator<Point>) new Comparator<Point>() {
				@Override
				public int compare(Point p1, Point p2) {
					return p1.compareTo(p2);
				}
			};
		}

		quickSortRec(0, points.length - 1);
	}

	/**
	 * Operates on the subarray of points[] with indices between first and last.
	 * 
	 * @param first starting index of the subarray
	 * @param last  ending index of the subarray
	 */
	private void quickSortRec(int first, int last) {
		if (first < last) {
			int p = partition(first, last);
			quickSortRec(first, p - 1);
			quickSortRec(p + 1, last);
		}
	}

	/**
	 * Operates on the subarray of points[] with indices between first and last.
	 * 
	 * @param first
	 * @param last
	 * @return
	 */
	private int partition(int first, int last) {
		Point pivot = points[last]; // choose the last element as pivot
		int i = first - 1; // index of smaller element

		for (int j = first; j < last; j++) {
			// If current element is smaller than or equal to pivot
			if (pointComparator.compare(points[j], pivot) <= 0) {
				i++;
				swap(i, j); // swap points[i] and points[j]
			}
		}

		swap(i + 1, last); // swap the pivot to its correct position
		return i + 1; // return the pivot index
	}

	// Other private methods if needed ...
}
