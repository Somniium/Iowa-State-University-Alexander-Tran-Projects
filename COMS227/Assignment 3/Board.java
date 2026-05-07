package hw3;

import static api.Direction.*;
import static api.Orientation.*;

import java.util.ArrayList;

import api.Cell;
import api.Direction;
import api.Move;
import api.Orientation;

/**
 * Represents a board in the game. A board contains a 2D grid of cells and a
 * list of boulders that slide over the cells.
 */
public class Board {
	/**
	 * 2D array of cells, the indexes signify (row, column) with (0, 0) representing
	 * the upper-left corner of the board.
	 */
	private Cell[][] grid;

	/**
	 * A list of boulders that are positioned on the board.
	 */
	private ArrayList<Boulder> boulders;

	/**
	 * A list of moves that have been made in order to get to the current position
	 * of boulders on the board.
	 */
	private ArrayList<Move> moveHistory;
	
	private ArrayList<Move> possibleMoves;
	private int moveCount = 0;
	private Boulder grabbedBoulder;
	private Cell cellBoulderGrabbed;
	private Cell cellMoved;
	private boolean isGameEnded;

	/**
	 * Constructs a new board from a given 2D array of cells and list of boulders.
	 * The cells of the grid should be updated to indicate which cells have boulders
	 * placed over them (i.e., setBoulder() method of Cell). The move history should
	 * be initialized as empty.
	 * 
	 * @param grid     a 2D array of cells which is expected to be a rectangular
	 *                 shape
	 * @param boulders list of boulders already containing row-column position which
	 *                 should be placed on the board
	 */
	public Board(Cell[][] grid, ArrayList<Boulder> boulders) {

		moveHistory = new ArrayList<Move>();

		this.boulders = boulders;
		this.grid = grid;

		for (Boulder b : boulders) {
			int length = b.getLength();
			int row = b.getFirstRow();
			int column = b.getFirstCol();

			this.grid[row][column].placeBoulder(b);

			if (b.getOrientation() == HORIZONTAL) {
				for (int i = column + 1; i < column + length; i++) {
					this.grid[row][i].placeBoulder(b);
				}
			} else if (b.getOrientation() == VERTICAL) {
				for (int i = row + 1; i < row + length; i++) {
					this.grid[i][column].placeBoulder(b);
				}
			}
		}
	}

	/**
	 * DO NOT MODIFY THIS CONSTRUCTOR
	 * <p>
	 * Constructs a new board from a given 2D array of String descriptions.
	 * 
	 * @param desc 2D array of descriptions
	 */
	public Board(String[][] desc) {
		this(GridUtil.createGrid(desc), GridUtil.findBoulders(desc));
	}

	/**
	 * Returns the number of rows of the board.
	 * 
	 * @return number of rows
	 */
	public int getRowSize() {
		return grid.length;
	}

	/**
	 * Returns the number of columns of the board.
	 * 
	 * @return number of columns
	 */
	public int getColSize() {
		return grid[0].length;
	}

	/**
	 * Returns the cell located at a given row and column.
	 * 
	 * @param row the given row
	 * @param col the given column
	 * @return the cell at the specified location
	 */
	public Cell getCellAt(int row, int col) {
		return grid[row][col];
	}

	/**
	 * Returns the total number of moves (calls to moveGrabbedBoulder which resulted
	 * in a boulder being moved) made so far in the game.
	 * 
	 * @return the number of moves
	 */
	public int getMoveCount() {
		return moveCount;
	}

	/**
	 * Returns a list of all boulders on the board.
	 * 
	 * @return a list of all boulders
	 */
	public ArrayList<Boulder> getBoulders() {
		return boulders;
	}

	/**
	 * Returns true if the player has completed the puzzle by positioning a boulder
	 * over an exit, false otherwise.
	 * 
	 * @return true if the game is over
	 */
	public boolean isGameOver() {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				Cell cell = grid[i][j];
				if (cell.isExit() && cell.hasBoulder() == true) {
					isGameEnded = true;
					return true;
				}
			}
		}
		isGameEnded = false;
		return isGameEnded;

	}

	/**
	 * Models the user grabbing (mouse button down) a boulder over the given row and
	 * column. The purpose of grabbing a boulder is for the user to be able to drag
	 * the boulder to a new position, which is performed by calling
	 * moveGrabbedBoulder().
	 * <p>
	 * This method should find which boulder has been grabbed (if any) and record
	 * that boulder as grabbed in some way.
	 * 
	 * @param row row to grab the boulder from
	 * @param col column to grab the boulder from
	 */
	public void grabBoulderAt(int row, int col) {
	    if (row < 0 || row >= grid.length || col < 0 || col >= grid[0].length) {
	        grabbedBoulder = null;
	        return;
	    }

	    Cell cell = grid[row][col];
	    if (cell.hasBoulder()) {
	        grabbedBoulder = cell.getBoulder();
	        cellBoulderGrabbed = cell;
	    } else {
	        grabbedBoulder = null;
	    }
	}
	/**
	 * Models the user releasing (mouse button up) the currently grabbed boulder (if
	 * any). Update the object accordingly to indicate no boulder is currently being
	 * grabbed.
	 */
	public void releaseBoulder() {
		grabbedBoulder = null;
	}

	/**
	 * Returns the currently grabbed boulder. If there is no currently grabbed
	 * boulder the method return null.
	 * 
	 * @return the currently grabbed boulder or null if none
	 */
	public Boulder getGrabbedBoulder() {
		return grabbedBoulder;
	}

	/**
	 * Returns true if the cell at the given row and column is available for a
	 * boulder to be placed over it. Boulders can only be placed over ground and
	 * exits. Additionally, a boulder cannot be placed over a cell that is already
	 * occupied by another boulder.
	 * 
	 * @param row row location of the cell
	 * @param col column location of the cell
	 * @return true if the cell is available for a boulder, otherwise false
	 */
	public boolean isAvailable(int row, int col) {
		Cell cell = grid[row][col];
		return (cell.isGround() || cell.isExit()) && !cell.hasBoulder();
	}

	/**
	 * Moves the currently grabbed boulder by one cell in the given direction. A
	 * horizontal boulder is only allowed to move right and left and a vertical
	 * boulder is only allowed to move up and down. A boulder can only move over a
	 * cell that is a floor or exit and is not already occupied by another boulder.
	 * The method does nothing under any of the following conditions:
	 * <ul>
	 * <li>The game is over.</li>
	 * <li>No boulder is currently grabbed by the user.</li>
	 * <li>A boulder is currently grabbed by the user, but the boulder is not
	 * allowed to move in the given direction.</li>
	 * </ul>
	 * If none of the above conditions are meet, the method does at least the
	 * following:
	 * <ul>
	 * <li>Moves the boulder object by calling its move() method.</li>
	 * <li>Calls placeBoulder() for the grid cell that the boulder is being moved
	 * into.</li>
	 * <li>Calls removeBoulder() for the grid cell that the boulder is being moved
	 * out of.</li>
	 * <li>Adds the move (as a Move object) to the end of the move history
	 * list.</li>
	 * <li>Increments the count of total moves made in the game.</li>
	 * </ul>
	 * 
	 * @param dir the direction to move
	 */
	public void moveGrabbedBoulder(Direction dir) {
		//IF game is over then return.
		if (isGameOver() || getGrabbedBoulder() == null) {
			return;
		}

		Boulder boulder = getGrabbedBoulder();
		int row = boulder.getFirstRow();
		int col = boulder.getFirstCol();
		int len = boulder.getLength();
		Orientation ori = boulder.getOrientation();

		if ((ori == HORIZONTAL && (dir != LEFT && dir != RIGHT)) || (ori == VERTICAL && (dir != UP && dir != DOWN))) {
			return;
		}

		//Checking for target location.
		int targetRow = row;
		int targetCol = col;
		if (dir == LEFT)
			targetCol--;
		else if (dir == RIGHT)
			targetCol += len;
		else if (dir == UP)
			targetRow--;
		else if (dir == DOWN)
			targetRow += len;

		//Checking bounds.
		if (targetRow < 0 || targetRow >= grid.length || targetCol < 0 || targetCol >= grid[0].length) {
			return;
		}

		Cell targetCell = grid[targetRow][targetCol];
		if (targetCell.isWall() || targetCell.hasBoulder()) {
			return;
		}

		//CLEARS current position.
		for (int i = 0; i < len; i++) {
			if (ori == HORIZONTAL) {
				grid[row][col + i].removeBoulder();
			} else {
				grid[row + i][col].removeBoulder();
			}
		}

		boulder.move(dir);

		//Update Position.
		int newRow = boulder.getFirstRow();
		int newCol = boulder.getFirstCol();
		for (int i = 0; i < len; i++) {
			if (ori == HORIZONTAL) {
				grid[newRow][newCol + i].placeBoulder(boulder);
			} else {
				grid[newRow + i][newCol].placeBoulder(boulder);
			}
		}

		// Update game state.
		moveHistory.add(new Move(boulder, dir));
		moveCount++;
		if (targetCell.isExit()) {
			isGameEnded = true;
		}
	}

	/**
	 * Resets the state of the game back to the start, which includes the move
	 * count, the move history, and whether the game is over. The method calls the
	 * reset method of each boulder object. It also updates each grid cells by
	 * calling their setBoulder method to either set a boulder if one is located
	 * over the cell or set null if no boulder is located over the cell.
	 */
	public void reset() {
		
		moveCount = 0;
		isGameEnded = false;
		moveHistory.clear();
		for (int i = 0; i < boulders.size(); i++) {
			boulders.get(i).reset();
		}

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				grid[i][j].removeBoulder();
			}
		}

		for (int i = 0; i < boulders.size(); i++) {
			int row = boulders.get(i).getFirstRow();
			int col = boulders.get(i).getFirstCol();
			int len = boulders.get(i).getLength();
			if (boulders.get(i).getOrientation() == HORIZONTAL) {
				for (int j = 0; j < len; j++) {
					grid[row][col + j].placeBoulder(boulders.get(i));
				}
			} else {
				for (int j = 0; j < len; j++) {
					grid[row + j][col].placeBoulder(boulders.get(i));
				}
			}
		}
	}

	/**
	 * Returns a list of all legal moves that can be made by any boulder on the
	 * current board.
	 * 
	 * @return a list of legal moves
	 */
	public ArrayList<Move> getAllPossibleMoves() {
		possibleMoves = new ArrayList<Move>();

		for (Boulder b : boulders) {
			int row = b.getFirstRow();
			int col = b.getFirstCol();
			int len = b.getLength();
			Orientation ori = b.getOrientation();
			
			//Checks all possible moves with each direction.
			if (ori == HORIZONTAL) {
				if (col + len < grid[0].length) {
					Cell rightCell = grid[row][col + len];
					if (!rightCell.hasBoulder() && !rightCell.isWall()) {
						possibleMoves.add(new Move(new Boulder(row, col, len, ori), Direction.RIGHT));
					}
				}
				if (col - 1 >= 0) {
					Cell leftCell = grid[row][col - 1];
					if (!leftCell.hasBoulder() && !leftCell.isWall()) {
						possibleMoves.add(new Move(new Boulder(row, col, len, ori), Direction.LEFT));
					}
				}
			} else if (ori == VERTICAL) {
				if (row + len < grid.length) {
					Cell downCell = grid[row + len][col];
					if (!downCell.hasBoulder() && !downCell.isWall()) {
						possibleMoves.add(new Move(new Boulder(row, col, len, ori), Direction.DOWN));
					}
				}
				if (row - 1 >= 0) {
					Cell upCell = grid[row - 1][col];
					if (!upCell.hasBoulder() && !upCell.isWall()) {
						possibleMoves.add(new Move(new Boulder(row, col, len, ori), Direction.UP));
					}
				}
			}
		}
		return possibleMoves;
	}

	/**
	 * Gets the list of all moves performed to get to the current position on the
	 * board.
	 * 
	 * @return a list of moves performed to get to the current position
	 */
	public ArrayList<Move> getMoveHistory() {
		return moveHistory;
	}

	/**
	 * EXTRA CREDIT 5 POINTS
	 * <p>
	 * This method is only used by the Solver.
	 * <p>
	 * Undo the previous move. The method gets the last move on the moveHistory list
	 * and performs the opposite actions of that move, which are the following:
	 * <ul>
	 * <li>if required, sets is game over to false</li>
	 * <li>grabs the moved boulder and calls moveGrabbedBoulder passing the opposite
	 * direction</li>
	 * <li>decreases the total move count by two to undo the effect of calling
	 * moveGrabbedBoulder twice</li>
	 * <li>removes the move from the moveHistory list</li>
	 * </ul>
	 * If the moveHistory list is empty this method does nothing.
	 */


	public void undoMove() {
		if (moveHistory.isEmpty()) {
			return;
		}

		isGameEnded = false;

		Move lastMove = moveHistory.remove(moveHistory.size() - 1);
		Boulder b = lastMove.getBoulder();
		Direction reverseDir = null;

		//Practicing cases over if-elses here.
		switch (lastMove.getDirection()) {
		case UP:
			reverseDir = Direction.DOWN;
			break;
		case DOWN:
			reverseDir = Direction.UP;
			break;
		case LEFT:
			reverseDir = Direction.RIGHT;
			break;
		case RIGHT:
			reverseDir = Direction.LEFT;
			break;
		}

		
		grabBoulderAt(b.getFirstRow(), b.getFirstCol());
		moveGrabbedBoulder(reverseDir);

		if (!moveHistory.isEmpty()) {
			moveHistory.remove(moveHistory.size() - 1);
		}
		//Subtracting by 2 since each logical move is equal to 2.
		moveCount -= 2;
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		boolean first = true;
		for (Cell row[] : grid) {
			if (!first) {
				buff.append("\n");
			} else {
				first = false;
			}
			for (Cell cell : row) {
				buff.append(cell.toString());
				buff.append(" ");
			}
		}
		return buff.toString();
	}
}
