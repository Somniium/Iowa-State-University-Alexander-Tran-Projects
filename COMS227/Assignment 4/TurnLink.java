package hw4;

import api.Point;

/**
 * A class that models a fixed link with three paths.
 * 
 * @author Alexander Tran
 */

public class TurnLink extends AbstractLink {

	private Point pointA;
	private Point pointC;

	/**
	 * Constructs a TurnLink connecting two active endpoints and one deadend.
	 * <p>
	 * pointA and pointC form the turn path. pointB exists purely to satisfy the
	 * three-path interface but acts as a deadend.
	 *
	 * @param pointA the entry/exit point at the base of the turn.
	 * @param pointC the entry/exit point at the end of the turn.
	 */
	public TurnLink(Point pointA, Point pointB, Point pointC) {
		this.pointA = pointA;
		this.pointC = pointC;
	}

	/**
	 * Returns the connected endpoint for a train entering this link.
	 * <p>
	 * Entering at pointA routes to pointC, and vice versa. Entering at pointB (or
	 * any unrecognized point) returns null.
	 *
	 * @param point the entry endpoint.
	 * @return the exit endpoint, or null for deadends.
	 */
	@Override
	public Point getConnectedPoint(Point point) {
		if (point == pointA) {
			return pointC;
		}
		if (point == pointC) {
			return pointA;
		}
		return null;
	}

	/**
	 * Returns the number of endpoints this link exposes.
	 *
	 * @return 3.
	 */
	@Override
	public int getNumPaths() {
		return 3;
	}
}
