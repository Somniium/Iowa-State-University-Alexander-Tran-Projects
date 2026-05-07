package hw3;

import static api.Orientation.*;
import static api.CellType.*;

import java.util.ArrayList;

import api.Cell;
import api.Orientation;

/**
 * Utilities for parsing string descriptions of a grid.
 */
public class GridUtil {
	/**
	 * Constructs a 2D grid of Cell objects given a 2D array of cell descriptions.
	 * String descriptions are a single character and have the following meaning.
	 * <ul>
	 * <li>"*" represents a wall.</li>
	 * <li>"e" represents an exit.</li>
	 * <li>"." represents a floor.</li>
	 * <li>"[", "]", "^", "v", or "#" represent a part of a boulder. A boulder is
	 * not a type of cell, it is something placed on a cell floor. For these
	 * descriptions a cell is created with CellType of FLOOR. This method does not
	 * create any boulders or set boulders on cells.</li>
	 * </ul>
	 * The method only creates cells and not boulders. See the other utility method
	 * findBoulders which is used to create the boulders.
	 * 
	 * @param desc a 2D array of strings describing the grid
	 * @return a 2D array of cells the represent the grid without any boulders
	 *         present
	 */
	public static Cell[][] createGrid(String[][] desc) {
		Cell[][] cell = new Cell[desc.length][desc[0].length];

		for (int i = 0; i < desc.length; i++) {
			for (int j = 0; j < desc[0].length; j++) {
				String temp = desc[i][j]; // To save up on space.
				if (temp.equals("[") || temp.equals("]") || temp.equals("^") || temp.equals("v")
						|| temp.equals("#") || temp.equals(".")) {
					cell[i][j] = new Cell(i, j, GROUND);
				} else if (temp.equals("*")) {
					cell[i][j] = new Cell(i, j, WALL);
				} else if (temp.equals("e")) {
					cell[i][j] = new Cell(i, j, EXIT);

				}
			}
		}
		return cell;
	}

	/**
	 * Returns a list of boulders that are constructed from a given 2D array of cell
	 * descriptions. String descriptions are a single character and have the
	 * following meanings.
	 * <ul>
	 * <li>"[" the start (left most column) of a horizontal boulder</li>
	 * <li>"]" the end (right most column) of a horizontal boulder</li>
	 * <li>"^" the start (top most row) of a vertical boulder</li>
	 * <li>"v" the end (bottom most column) of a vertical boulder</li>
	 * <li>"#" inner segments of a boulder, these are always placed between the
	 * start and end of the boulder</li>
	 * <li>"*", ".", and "e" symbols that describe cell types, meaning there is not
	 * boulder currently over the cell</li>
	 * </ul>
	 * 
	 * @param desc a 2D array of strings describing the grid
	 * @return a list of boulders found in the given grid description
	 */
	public static ArrayList<Boulder> findBoulders(String[][] desc) {
		ArrayList<Boulder> boulders = new ArrayList<>();

		// Horizontal boulders
		for (int i = 0; i < desc.length; i++) {
			for (int j = 0; j < desc[i].length; j++) {
				if (desc[i][j].equals("[")) {
					int boulderLength = 1;
					int boulderRow = i;
					int boulderCol = j;
					int l = j + 1;

					while (l < desc[i].length && (desc[i][l].equals("#") || desc[i][l].equals("]"))) {
						boulderLength++;

						if (desc[i][l].equals("]")) {
							boulders.add(new Boulder(boulderRow, boulderCol, boulderLength, HORIZONTAL));
							break;
						}
						l++;
					}
				}
			}
		}

		// Vertical boulders
		for (int j = 0; j < desc[0].length; j++) {
			for (int i = 0; i < desc.length; i++) {
				if (desc[i][j].equals("^")) {
					int boulderLength = 1;
					int boulderRow = i;
					int boulderCol = j;
					int l = i + 1;

					while (l < desc.length && (desc[l][j].equals("#") || desc[l][j].equals("v"))) {
						boulderLength++;

						if (desc[l][j].equals("v")) {
							boulders.add(new Boulder(boulderRow, boulderCol, boulderLength, VERTICAL));
							break;
						}
						l++;
					}
				}
			}
		}
		return boulders;
	}
}