package hw4;

import api.Point;

/**
 * A class that Models a switchable link with three paths. A boolean turn
 * determines which path trains take. By default turn is set to false.
 * 
 * @author Alexander Tran
 */
public class SwitchLink extends AbstractLink {
	private boolean turn;
	private Point pointA;
	private Point pointB;
	private Point pointC;

	/**
	 * Constructs a switchable link between three points.
	 * <p>
	 * By default, turn is false, going from pointA to pointB.
	 *
	 * @param pointA the entry/exit point.
	 * @param pointB the first branch point.
	 * @param pointC the second branch point.
	 */
	public SwitchLink(Point pointA, Point pointB, Point pointC) {
		this.pointA = pointA;
		this.pointB = pointB;
		this.pointC = pointC;
		this.turn = false;
	}

	/**
	 * Sets the turn for this link.
	 * 
	 * @param turn false to connect pointA to pointB, or true to connect pointA to
	 *             pointC.
	 */
	public void setTurn(boolean turn) {
		this.turn = turn;
	}

	/**
	 * Returns the connected endpoint for a train entering at the given point.
	 * <p>
	 * If entering at pointA, routes to pointB or pointC depending on turn. All
	 * other inputs yield null.
	 *
	 * @param point the entry endpoint.
	 * @return the exit endpoint, or null if the point is unsupported.
	 */
	@Override
	public Point getConnectedPoint(Point point) {
		if (!turn) {
			// Switch set to branch B.
			if (point == pointA) {
				return pointB;
			}
			if (point == pointB) {
				return pointA;
			}
		} else {
			// Switch set to branch C.
			if (point == pointA) {
				return pointC;
			}
			if (point == pointC) {
				return pointA;
			}
		}
		// Any other endpoint is a dead end.
		return null;
	}

	/**
	 * Returns the total number of endpoints exposed by this link.
	 *
	 * @return 3.
	 */
	@Override
	public int getNumPaths() {
		return 3;
	}
}
