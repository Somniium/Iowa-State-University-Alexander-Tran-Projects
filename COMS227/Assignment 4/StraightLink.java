package hw4;

import api.Point;

/**
 * A class that models a fixed link with three paths.
 * 
 * @author Alexander Tran
 */
public class StraightLink extends AbstractLink {
	private Point pointA;
	private Point pointB;

	/**
	 * Constructor for StraightLink.
	 * 
	 * PointC shouldn't be needed since SwitchLink and TurnLink can inherently
	 * handle the PointC interactions.
	 * 
     * @param pointA one endpoint of the straight segment.
     * @param pointB the opposite endpoint of the straight segment.
	 */
	public StraightLink(Point pointA, Point pointB, Point pointC) {
		this.pointA = pointA;
		this.pointB = pointB;
	}

	/**
	 * Returns the opposite endpoint on the straight path.
	 * <p>
	 * If entered at pointA, exits at pointB, and vice versa. Any entry at
	 * pointC (or unknown points) returns null.
	 *
	 * @param point the initial endpoint.
	 * @return the opposite endpoint.
	 */
	@Override
	public Point getConnectedPoint(Point point) {
		if (point == pointA) {
			return pointB;
		} else {
			return pointA;
		}
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
