package hw4;

import api.Crossable;
import api.PositionVector;

/**
 * A class that models a link that connects a single path to nothing.
 * 
 * @author Alexander Tran
 */

/**
 * A link representing a dead end where a path terminates.
 */
public class DeadEndLink extends AbstractLink implements Crossable {

	/**
	 * Constructs a dead-end link. There is no additional state to initialize.
	 */
	public DeadEndLink() {
	}

	/**
	 * shiftPoints in this class will do nothing because there is no next segment
	 * after a deadend.
	 * 
	 * @param positionVector the train's current position vector (unused).
	 */
	@Override
	public void shiftPoints(PositionVector positionVector) {
	}

	/**
	 * Returns the number of paths provided by this link.
	 * 
	 * Always one, since it terminates a single track segment.
	 *
	 * @return 1.
	 */
	@Override
	public int getNumPaths() {
		return 1;
	}

}