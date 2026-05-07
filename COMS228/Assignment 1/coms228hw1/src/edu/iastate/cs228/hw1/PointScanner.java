package edu.iastate.cs228.hw1;

/**
 * 
 * @author Alexander Tran
 *
 */

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.File;

/**
 * 
 * This class sorts all the points in an array of 2D points to determine a
 * reference point whose x and y coordinates are respectively the medians of the
 * x and y coordinates of the original points.
 * 
 * It records the employed sorting algorithm as well as the sorting time for
 * comparison.
 *
 */
public class PointScanner {
	private Point[] points;

	private Point medianCoordinatePoint; // point whose x and y coordinates are respectively the medians of
											// the x coordinates and y coordinates of those points in the array
											// points[].
	private Algorithm sortingAlgorithm;

	protected long scanTime; // execution time in nanoseconds.

	/**
	 * This constructor accepts an array of points and one of the four sorting
	 * algorithms as input. Copy the points into the array points[].
	 * 
	 * @param pts input array of points
	 * @throws IllegalArgumentException if pts == null or pts.length == 0.
	 */
	public PointScanner(Point[] pts, Algorithm algo) throws IllegalArgumentException {
		if (pts == null || pts.length == 0) {
			throw new IllegalArgumentException("Points are null or is 0 in length");
		}

		points = new Point[pts.length];
		for (int i = 0; i < pts.length; i++) {
			points[i] = pts[i];
		}
		this.sortingAlgorithm = algo;
	}

	/**
	 * This constructor reads points from a file.
	 * 
	 * @param inputFileName
	 * @throws FileNotFoundException
	 * @throws InputMismatchException if the input file contains an odd number of
	 *                                integers
	 */
	protected PointScanner(String inputFileName, Algorithm algo) throws FileNotFoundException, InputMismatchException {
		Scanner scnr = new Scanner(new File(inputFileName));
		ArrayList<Integer> numbers = new ArrayList<>();

		while (scnr.hasNextInt()) {
			numbers.add(scnr.nextInt());
		}
		scnr.close();

		if (numbers.size() % 2 != 0) {
			throw new InputMismatchException("Input file contains an odd number of integers");
		}

		points = new Point[numbers.size() / 2];
		for (int i = 0; i < numbers.size(); i += 2) {
			points[i / 2] = new Point(numbers.get(i), numbers.get(i + 1));
		}

		this.sortingAlgorithm = algo;
	}

	/**
	 * Carry out two rounds of sorting using the algorithm designated by
	 * sortingAlgorithm as follows:
	 * 
	 * a) Sort points[] by the x-coordinate to get the median x-coordinate. b) Sort
	 * points[] again by the y-coordinate to get the median y-coordinate. c)
	 * Construct medianCoordinatePoint using the obtained median x- and
	 * y-coordinates.
	 * 
	 * Based on the value of sortingAlgorithm, create an object of SelectionSorter,
	 * InsertionSorter, MergeSorter, or QuickSorter to carry out sorting.
	 * 
	 * @param algo
	 * @return
	 */
	public void scan() {
		if (sortingAlgorithm == null || points == null || points.length == 0) {
			return;
		}

		AbstractSorter aSorter;
		switch (sortingAlgorithm) {
		case InsertionSort:
			aSorter = new InsertionSorter(points);
			break;
		case MergeSort:
			aSorter = new MergeSorter(points);
			break;
		case QuickSort:
			aSorter = new QuickSorter(points);
			break;
		case SelectionSort:
			aSorter = new SelectionSorter(points);
			break;
		default:
			return;
		}

		long totalTime = 0;
		int[] medianCoords = new int[2]; // [0] for x, [1] for y

		for (int round = 0; round < 2; round++) {
			aSorter.setComparator(round);

			long startTime = System.nanoTime();
			aSorter.sort();
			long endTime = System.nanoTime();
			totalTime += (endTime - startTime);

			// Handle both even and odd array lengths for median
			int mid = points.length / 2;
			if (points.length % 2 == 0) {
				// Use getX() for round 0 (x-coordinate) and getY() for round 1 (y-coordinate)
				if (round == 0) {
					medianCoords[round] = (points[mid - 1].getX() + points[mid].getX()) / 2;
				} else {
					medianCoords[round] = (points[mid - 1].getY() + points[mid].getY()) / 2;
				}
			} else {
				// Use getX() for round 0 (x-coordinate) and getY() for round 1 (y-coordinate)
				if (round == 0) {
					medianCoords[round] = points[mid].getX();
				} else {
					medianCoords[round] = points[mid].getY();
				}
			}
		}

		medianCoordinatePoint = new Point(medianCoords[0], medianCoords[1]);
		scanTime = totalTime;
	}

	/**
	 * Outputs performance statistics in the format:
	 * 
	 * <sorting algorithm> <size> <time>
	 * 
	 * For instance,
	 * 
	 * selection sort 1000 9200867
	 * 
	 * Use the spacing in the sample run in Section 2 of the project description.
	 */
	public String stats() {
		if (sortingAlgorithm == null) {
			return null;
		}

		String algorithmName;
		switch (sortingAlgorithm) {
		case InsertionSort:
			algorithmName = "insertion sort";
			break;
		case MergeSort:
			algorithmName = "merge sort";
			break;
		case QuickSort:
			algorithmName = "quick sort";
			break;
		case SelectionSort:
			algorithmName = "selection sort";
			break;
		default:
			return null;
		}
		return String.format("%-17s %-10d %-10d", algorithmName, points.length, scanTime);
	}

	/**
	 * Write MCP after a call to scan(), in the format "MCP: (x, y)" The x and y
	 * coordinates of the point are displayed on the same line with exactly one
	 * blank space in between.
	 */
	@Override
	public String toString() {
		return "MCP:" + medianCoordinatePoint.toString();
	}

	/**
	 * 
	 * This method, called after scanning, writes point data into a file by
	 * outputFileName. The format of data in the file is the same as printed out
	 * from toString(). The file can help you verify the full correctness of a
	 * sorting result and debug the underlying algorithm.
	 * 
	 * @throws FileNotFoundException
	 */
	public void writeMCPToFile() throws FileNotFoundException {
		if (points == null || points.length == 0) {
			throw new IllegalStateException("No points are available to write");
		}
		PrintWriter w = new PrintWriter("MCP.txt");
		w.println(toString());
		w.close();

	}

}
