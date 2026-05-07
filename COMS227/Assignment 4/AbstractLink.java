package hw4;

import api.Crossable;
import api.Point;
import api.PositionVector;

/**
 * An abstract class that models a link between paths.
 * 
 * @author Alexander Tran
 */

public abstract class AbstractLink implements Crossable {

	public AbstractLink() {
	}

	@Override
	/**
	 * Gets the point that is connected to the given point by the link. Returns null
	 * if no point is connected to the given point.
	 * 
	 * @param point the given point.
	 * @return the connected point or null.
	 */
	public Point getConnectedPoint(Point point) {
		return null;
	}

	/**
	 * This method is called by the simulation to indicate a train has entered the
	 * crossing.
	 */
	@Override
	public void trainEnteredCrossing() {
	}

	/**
	 * This method is called by the simulation to indicate a train has exited the
	 * crossing.
	 */
	@Override
	public void trainExitedCrossing() {
	}

	/**
	 * Shift the location of the given positionVector to be between the next pair of
	 * points.
	 * <p>
	 * For example, suppose the vector is currently at the end of path 1 on points A
	 * and B. Assume endpoint B on path 1 is linked to endpoint C on path 2. Then
	 * the positionVector will be updated to be between points C and D, where D is
	 * the next point on path 2 after C.
	 * <p>
	 * The method does not change the relative distance between the points, it only
	 * modifies the points.
	 */
	public void shiftPoints(PositionVector positionVector) {
		Point connectedPoint = getConnectedPoint(positionVector.getPointB());
		if (connectedPoint == null) {
			// Dead end case
			return;
		}

		Point nextPoint;
		if (connectedPoint.getPointIndex() == 0) {
			nextPoint = connectedPoint.getPath().getPointByIndex(1);
		} else {
			nextPoint = connectedPoint.getPath().getPointByIndex(connectedPoint.getPointIndex() - 1);
		}

		positionVector.setPointA(connectedPoint);
		positionVector.setPointB(nextPoint);
	}

	/**
	 * Gets the total number of paths connected by the link.
	 * 
	 * @return the total number of paths.
	 */
	@Override
	public int getNumPaths() {
		return 0;
	}
}