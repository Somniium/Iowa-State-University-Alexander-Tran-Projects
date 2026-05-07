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
 * This class implements the mergesort algorithm.
 *
 */

public class MergeSorter extends AbstractSorter {
	// Other private instance variables if needed

	/**
	 * Constructor takes an array of points. It invokes the superclass constructor,
	 * and also set the instance variables algorithm in the superclass.
	 * 
	 * @param pts input array of integers
	 */
	public MergeSorter(Point[] pts) {
		super(pts);
		algorithm = "Merge Sort";
	}

	/**
	 * Perform mergesort on the array points[] of the parent class AbstractSorter.
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
					return p1.compareTo(p2); // Uses Point's compareTo which respects xORy
				}
			};
		}

		if (points == null || points.length == 0) {
			return;
		}
		mergeSortRec(points);
	}

	/**
	 * This is a recursive method that carries out mergesort on an array pts[] of
	 * points. One way is to make copies of the two halves of pts[], recursively
	 * call mergeSort on them, and merge the two sorted subarrays into pts[].
	 * 
	 * @param pts point array
	 */
	/**
	 * This is a recursive method that carries out mergesort on an array pts[] of
	 * points. One way is to make copies of the two halves of pts[], recursively
	 * call mergeSort on them, and merge the two sorted subarrays into pts[].
	 * 
	 * @param pts point array
	 */
	private void mergeSortRec(Point[] pts) {
		if (pts.length <= 1) { // Recursive base case
			return;
		}
		int mid = pts.length / 2;
		Point[] left = new Point[mid];
		Point[] right = new Point[pts.length - mid];

		int i = 0;

		for (int j = 0; j < left.length; j++) {
			left[j] = pts[i++];
		}
		for (int j = 0; j < right.length; j++) {
			right[j] = pts[i++];
		}

		mergeSortRec(left);
		mergeSortRec(right);
		merge(pts, left, right);
	}

	// Other private methods if needed ...
	private void merge(Point[] arr, Point[] left, Point[] right) {
		int l = 0;
		int r = 0; 
		int i = 0;
		
		while (l < left.length && r < right.length) {
			if (pointComparator.compare(left[l], right[r]) <= 0) {
				arr[i++] = left[l++];
			} else {
				arr[i++] = right[r++];
			}
		}
		while (l < left.length) {
			arr[i++] = left[l++];
		}
		while (r < right.length) {
			arr[i++] = right[r++];
		}
	}
}
