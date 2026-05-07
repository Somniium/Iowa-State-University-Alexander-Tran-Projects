package hw4;

import api.Point;
import api.PointPair;

/**
 * A switchable version of MultiFixedLink with 2 to 6 PointPair connections.
 * Allows changing individual PointPairs unless a train is currently crossing.
 *
 * @author Alexander Tran
 */
public class MultiSwitchLink extends AbstractLink {

	private PointPair[] connections;
	private boolean isCrossing;

	/**
	 * Constructs a MultiSwitchLink with a fixed-size set of connections.
	 *
	 * @param connections array of PointPairs (2 to 6 total).
	 * @throws IllegalArgumentException if the array length is less than 2 or
	 *                                  greater than 6.
	 */
	public MultiSwitchLink(PointPair[] connections) {
		if (connections.length < 2 || connections.length > 6) {
			throw new IllegalArgumentException("Must have between 2 and 6 point pairs");
		}
		this.connections = connections;
		this.isCrossing = false;
	}

	/**
	 * Replaces the PointPair at the specified index with a new connection, provided
	 * no train is currently traversing this link.
	 *
	 * @param newConnection the replacement PointPair
	 * @param index         the index of the connection to replace.
	 */
	public void switchConnection(PointPair newConnection, int index) {
		if (!isCrossing && index >= 0 && index < connections.length) {
			connections[index] = newConnection;
		}
	}

	/**
	 * Checks if a train has entered a crossing, until trainExitedCrossing is
	 * called.
	 */
	@Override
	public void trainEnteredCrossing() {
		isCrossing = true;
	}

	/**
	 * Checks if a train has exited a crossing, until trainEnteredCrossing is
	 * called.
	 */
	@Override
	public void trainExitedCrossing() {
		isCrossing = false;
	}

	/**
	 * Finds and returns the Point at the opposite end of the given entry point.
	 * <p>
	 * Searches all configured PointPairs; if the input matches one endpoint, the
	 * other is returned. If no match exists, returns null.
	 *
	 * @param point the entry endpoint.
	 * @return the connected exit endpoint, or null if not found.
	 */
	@Override
	public Point getConnectedPoint(Point point) {
		for (PointPair pair : connections) {
			if (point == pair.getPointA()) {
				return pair.getPointB();
			}
			if (point == pair.getPointB()) {
				return pair.getPointA();
			}
		}
		return null;
	}

	/**
	 * Returns the total number of available traversal paths.
	 *
	 * @return connections.length * 2.
	 */
	@Override
	public int getNumPaths() {
		return connections.length * 2;
	}
}
