package hw4;

import api.Point;

/**
 * A class that models a link between exactly two paths.
 * 
 * @author Alexander Tran
 */
public class CouplingLink extends AbstractLink {
	private Point pointA;
	private Point pointB;

	/**
	 * Constructs a CouplingLink connecting two points.
	 *
	 * @param pointA one endpoint of the link.
	 * @param pointB the other endpoint of the link.
	 */
	public CouplingLink(Point pointA, Point pointB) {
		this.pointA = pointA;
		this.pointB = pointB;
	}
	
	
    /**
     * Returns the point connected to the given input point.
     * <p>
     * Traversal is bi-directional: entering at pointA exits at pointB,
     * and vice versa. All other inputs return null.
     *
     * @param point the endpoint from which traversal begins.
     * @return the opposite endpoint, or null if input is not part of this link.
     */
	@Override
	public Point getConnectedPoint(Point point) {
		if (point == pointA) {
			return pointB;
		}
		if (point == pointB) {
			return pointA;
		}
		return null;
	}

    /**
     * Returns the number of direct paths this link provides.
     *
     * @return 2.
     */
	@Override
	public int getNumPaths() {
		return 2;
	}
}
