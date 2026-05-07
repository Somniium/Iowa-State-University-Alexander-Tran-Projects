package hw4;

import api.Point;
import api.PointPair;

/**
 * A class that models a link with a fixed set of 2 to 6 path connections.
 *
 * Each PointPair represents a two-way connection between two endpoints.
 *
 * @author Alexander Tran
 */
public class MultiFixedLink extends AbstractLink {

	private PointPair[] connections;

	/**
	 * Constructs a MultiFixedLink with a fixed array of point pairs.
	 *
	 * @param connections array of PointPair objects (must be 2 to 6 in length)
	 * @throws IllegalArgumentException if the array length is less than 2 or
	 *                                  greater than 6.
	 */
	public MultiFixedLink(PointPair[] connections) {
		if (connections.length < 2 || connections.length > 6) {
			throw new IllegalArgumentException("Must have between 2 and 6 point pairs");
		}
		this.connections = connections;
	}

	/**
	 * Finds the partner of the given point by searching every PointPair in this
	 * link. If the input matches one end of a pair, the opposite end is returned,
	 * otherwise null.
	 *
	 * @param point the endpoint to look up.
	 * @return the connected endpoint, or null if no connection exists.
	 */
	@Override
	public Point getConnectedPoint(Point point) {
		Point pointTemp = null;
		for (PointPair pair : connections) {
			if (point == pair.getPointA()) {
				pointTemp = pair.getPointB();
			}
			if (point == pair.getPointB()) {
				pointTemp = pair.getPointA();
			}
		}
		return pointTemp;
	}
}
