# Sorting 2D Integer Points

A Java sorting and algorithm comparison project created for COM S 2280.  
This project reads or generates 2D integer points, sorts them using multiple sorting algorithms, and finds the median coordinate point of the dataset.

## Project Overview

This project focuses on sorting arrays of 2D points and comparing the performance of different sorting algorithms. The program finds a median coordinate point by sorting the same set of points twice: once by x-coordinate and once by y-coordinate.

The median coordinate point is not necessarily one of the original input points. Instead, it is created from the median x-coordinate and the median y-coordinate of the input set.

The project supports four sorting algorithms:

- Selection sort
- Insertion sort
- Merge sort
- Quicksort

Each algorithm is tested on the same set of points so their running times can be compared.

## Features

- Represents 2D integer points using a `Point` class
- Supports duplicate points
- Compares points by x-coordinate or y-coordinate
- Uses tie-breaking when coordinates are equal
- Implements four sorting algorithms
- Uses an abstract superclass for shared sorting behavior
- Reads point data from arrays
- Reads point data from input files
- Generates random points within a fixed coordinate range
- Finds the median coordinate point
- Measures sorting time in nanoseconds
- Compares algorithm performance in a formatted table

## Technologies Used

- Java
- Object-oriented programming
- Abstract classes
- Inheritance
- Comparators
- Arrays
- File input
- `Scanner`
- `Random`
- Javadoc

## Main Classes

### `Point`

Represents a 2D point with integer x and y coordinates.

The `Point` class implements `Comparable` and compares points by either x-coordinate or y-coordinate. If two points have the same value for the selected coordinate, the other coordinate is used as a tie-breaker.

### `AbstractSorter`

An abstract superclass for all sorter classes.

Main responsibilities:

- Store the array of points
- Copy input points into an internal array
- Store the name of the sorting algorithm
- Set the comparator for sorting by x-coordinate or y-coordinate
- Return the sorted points
- Return the median point after sorting

### `SelectionSorter`

Implements selection sort for an array of points.

### `InsertionSorter`

Implements insertion sort for an array of points.

### `MergeSorter`

Implements merge sort for an array of points.

### `QuickSorter`

Implements quicksort for an array of points.

### `PointScanner`

Scans a set of points using one sorting algorithm.

Main responsibilities:

- Accept points from an array or input file
- Sort the points by x-coordinate
- Sort the points by y-coordinate
- Determine the median coordinate point
- Track the total scan time
- Format scan statistics for output

### `CompareSorters`

Runs the main program and compares all four sorting algorithms.

Main responsibilities:

- Let the user choose random point generation, file input, or exit
- Generate random points when requested
- Read points from a file when requested
- Run all four sorting algorithms on the same input
- Print aligned performance results

## Median Coordinate Point

The median coordinate point is found by sorting the points twice:

1. Sort the points by x-coordinate and take the median x-value.
2. Sort the points by y-coordinate and take the median y-value.
3. Combine those two median values into one point.

For example, if the median x-coordinate is `0` and the median y-coordinate is `1`, then the median coordinate point is:

```text
(0, 1)
```

This point may or may not already exist in the original input set.

## Example Input File

An input file contains integers where every pair of integers represents one point.

Example:

```text
0 0 -3 -9 0 -10
8 4 3 3 -6
3 -2 1
10 5 -7 -10
```

This would be read as pairs of coordinates:

```text
(0, 0)
(-3, -9)
(0, -10)
(8, 4)
(3, 3)
(-6, 3)
(-2, 1)
(10, 5)
(-7, -10)
```

## Example Program Output

```text
Performances of Four Sorting Algorithms in Point Scanning

keys: 1 (random integers) 2 (file input) 3 (exit)
Trial 1: 1
Enter number of random points: 1000

algorithm       size    time (ns)
---------------------------------
SelectionSort   1000    49631547
InsertionSort   1000    22604220
MergeSort       1000    2057874
QuickSort       1000    1537183
---------------------------------
```

## What I Learned

Through this project, I practiced implementing and comparing classic sorting algorithms in Java. I also gained more experience with inheritance by using an abstract sorter class and extending it with multiple specific sorting implementations.

This project also helped me understand how sorting can be used as part of a larger problem. Instead of sorting just for the sake of sorting, the program uses sorted point data to calculate the median coordinate point.

## Challenges

One challenge was making sure points were compared correctly depending on whether the current scan was sorting by x-coordinate or y-coordinate. The tie-breaking logic also had to be consistent so the sorting algorithms behaved correctly.

Another challenge was keeping the sorting algorithms separate while still sharing common code through the abstract superclass. This made the design cleaner and reduced repeated code across the sorter classes.

File input was also important because the program needed to correctly read coordinate pairs and detect invalid input when a file contained an odd number of integers.

## How to Run

1. Place the source files in the package:

```text
edu.iastate.cs228.hw1
```

2. Compile the project in your Java IDE or from the command line.

3. Run the `CompareSorters` class.

4. Choose one of the menu options:

```text
1 - Generate random points
2 - Read points from a file
3 - Exit
```

5. View the timing results for each sorting algorithm.

## Random Point Generation

Random points are generated within the coordinate range:

```text
[-50, 50] x [-50, 50]
```

This means each x-coordinate and y-coordinate is an integer between `-50` and `50`, inclusive.

## Author

Alexander Tran
