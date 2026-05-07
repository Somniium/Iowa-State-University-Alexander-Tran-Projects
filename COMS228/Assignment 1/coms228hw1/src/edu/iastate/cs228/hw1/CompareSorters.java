package edu.iastate.cs228.hw1;

/**
 *  
 * @author Alexander Tran
 *
 */

/**
 * 
 * This class executes four sorting algorithms: selection sort, insertion sort, mergesort, and
 * quicksort, over randomly generated integers as well integers from a file input. It compares the 
 * execution times of these algorithms on the same input. 
 *
 */

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Random;

public class CompareSorters {
	/**
	 * Repeatedly take integer sequences either randomly generated or read from
	 * files. Use them as coordinates to construct points. Scan these points with
	 * respect to their median coordinate point four times, each time using a
	 * different sorting algorithm.
	 * 
	 * @param args
	 **/
	public static void main(String[] args) throws FileNotFoundException {
		Scanner scnr = new Scanner(System.in);
		Random rand = new Random();
		int trial = 1;

		System.out.println("Performances of Four Sorting Algorithms in Point Scanning");
		System.out.println();
		System.out.println("keys: 1 (random integers) 2 (file input) 3 (exit)");

		while (true) {
			System.out.print("Trial " + trial + ": ");
			int choice = scnr.nextInt();

			if (choice == 3) {
				System.out.println("Exit");
				break;
			}

			Point[] points = null;
			String filename = "";

			if (choice == 1) {
				System.out.print("Enter number of random points: ");
				int numPts = scnr.nextInt();
				points = generateRandomPoints(numPts, rand);
			} else if (choice == 2) {
				System.out.print("Points from a file\nFile name:");
				filename = scnr.next();
			} else {
				System.out.println("Invalid choice");
				continue;
			}

			PointScanner[] scnrs = new PointScanner[4];
			Algorithm[] algorithms = Algorithm.values();

			for (int i = 0; i < 4; i++) {
				if (choice == 1) {
					scnrs[i] = new PointScanner(points, algorithms[i]);
				} else {
					scnrs[i] = new PointScanner(filename, algorithms[i]);
				}
			}

			System.out.println("\nalgorithm         size       time (ns)");
			System.out.println("----------------------------------");

			for (PointScanner s : scnrs) {
				s.scan();
				System.out.println(s.stats());
			}

			System.out.println("----------------------------------\n");
			trial++;
		}
		scnr.close();
	}

	//
	// Conducts multiple rounds of comparison of four sorting algorithms. Within
	// each round,
	// set up scanning as follows:
	//
	// a) If asked to scan random points, calls generateRandomPoints() to initialize
	// an array
	// of random points.
	//
	// b) Reassigns to the array scanners[] (declared below) the references to four
	// new
	// PointScanner objects, which are created using four different values
	// of the Algorithm type: SelectionSort, InsertionSort, MergeSort and QuickSort.
	//
	//

	// For each input of points, do the following.
	//
	// a) Initialize the array scanners[].
	//
	// b) Iterate through the array scanners[], and have every scanner call the
	// scan()
	// method in the PointScanner class.
	//
	// c) After all four scans are done for the input, print out the statistics
	// table from
	// section 2.
	//
	// A sample scenario is given in Section 2 of the project description.

	/**
	 * This method generates a given number of random points. The coordinates of
	 * these points are pseudo-random numbers within the range [-50,50] � [-50,50].
	 * Please refer to Section 3 on how such points can be generated.
	 * 
	 * Ought to be private. Made public for testing.
	 * 
	 * @param numPts number of points
	 * @param rand   Random object to allow seeding of the random number generator
	 * @throws IllegalArgumentException if numPts < 1
	 */
	public static Point[] generateRandomPoints(int numPts, Random rand) throws IllegalArgumentException {
		if (numPts < 1) {
			throw new IllegalArgumentException("Number of points must be at least 1");
		}

		Point[] points = new Point[numPts];
		for (int i = 0; i < numPts; i++) {
			int x = rand.nextInt(101) - 50; // -50 to 50
			int y = rand.nextInt(101) - 50; // -50 to 50
			points[i] = new Point(x, y);
		}
		return points;
	}
}
