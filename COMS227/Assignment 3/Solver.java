package hw3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import api.Move;

/**
 * A puzzle solver for the the game.
 * <p>
 * THE ONLY METHOD YOU ARE CHANGING IN THIS CLASS IS solve().
 */
public class Solver {
	/**
	 * Maximum number of moves allowed in the search.
	 */
	private int maxMoves;

	/**
	 * Associates a string representation of a grid with the move count required to
	 * reach that grid layout.
	 */
	private Map<String, Integer> seen = new HashMap<String, Integer>();

	/**
	 * All solutions found in this search.
	 */
	private ArrayList<ArrayList<Move>> solutions = new ArrayList<ArrayList<Move>>();

	/**
	 * Constructs a solver with the given maximum number of moves.
	 * 
	 * @param givenMaxMoves maximum number of moves
	 */
	public Solver(int givenMaxMoves) {
		maxMoves = givenMaxMoves;
		solutions = new ArrayList<ArrayList<Move>>();
	}

	/**
	 * Returns all solutions found in the search. Each solution is a list of moves.
	 * 
	 * @return list of all solutions
	 */
	public ArrayList<ArrayList<Move>> getSolutions() {
		return solutions;
	}

	/**
	 * Prints all solutions found in the search.
	 */
	public void printSolutions() {
		for (ArrayList<Move> moves : solutions) {
			System.out.println("Solution:");
			for (Move move : moves) {
				System.out.println(move);
			}
			System.out.println();
		}
	}

	/**
	 * EXTRA CREDIT 10 POINTS
	 * <p>
	 * Recursively search for solutions to the given board instance according to the
	 * algorithm described in the assignment pdf. This method does not return
	 * anything its purpose is to update the instance variable solutions with every
	 * solution found.
	 * 
	 * @param board any instance of Board
	 */
	/**
	 * Recursively searches for solutions to the given board configuration.
	 * Updates the solutions list with all valid solutions found.
	 * 
	 * @param board the current board state to explore
	 */
	public void solve(Board board) {
	    //2 Base cases: 
		//1 IF moveCount has exceeded exceeded allowed moves.
		//2 IF a solution has been found I.E game is over.
	    if (board.getMoveCount() > maxMoves) {
	        return;
	    }
	    if (board.isGameOver()) {
	        solutions.add(new ArrayList<>(board.getMoveHistory()));
	        return;
	    }

	    //Check if we've seen this board state before with fewer moves.
	    String boardKey = board.toString();
	    Integer previousMoveCount = seen.get(boardKey);
	    
	    if (previousMoveCount != null && board.getMoveCount() >= previousMoveCount) {
	        return; 
	    }
	    
	    //Record this board state with current move count.
	    seen.put(boardKey, board.getMoveCount());

	    //Explore all possible moves.
	    for (Move move : board.getAllPossibleMoves()) {
	        board.grabBoulderAt(move.getBoulder().getFirstRow(), move.getBoulder().getFirstCol());
	        board.moveGrabbedBoulder(move.getDirection());
	        solve(board);
	        board.undoMove();
	    }
	}
}
