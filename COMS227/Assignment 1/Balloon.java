package hw1;

/**
 * The balloon class simulates the behavior of a balloon that can move
 * vertically and horitzontally, consume fuel, altitude, and various other small
 * functions that make up a balloon.
 * 
 * @author Alexander Tran
 */
public class Balloon {
	private int verticalVelocity; 	// Current vertical speed
	private int horizontalVelocity; // Current horizontal speed
	private int altitude; 			// Current altitude of balloon
	private int maxAltitude; 		// Maximum altitude of baloon
	private int groundPosition; 	// Position of balloon along the ground
	private int maxGroundPosition; 	// Maximum position of balloon on the ground
	private double fuel; 			// Amount of fuel in the balloon
	private double tankCapacity; 	// Amount of fuel capacity in the balloon
	private double maxBurnRate; 	// Maximum amount of fuel that can be burned in one second
	private int timeSeconds; 		// Amount of time within

	/**
	 * Constructs a new Balloon with the specified initial ground position, maximum
	 * ground position, maximum altitude, and fuel tank capacity.
	 *
	 * @param groundPosition 	The initial ground position of the balloon.
	 * @param maxGroundPosition The maximum ground position (the balloon will wrap around within 0 to maxGroundPosition-1).
	 * @param maxAltitude 		The maximum altitude the balloon can achieve.
	 * @param tankCapacity 		The fuel tank capacity.
	 */
	public Balloon(int groundPosition, int maxGroundPosition, int maxAltitude, double tankCapacity) {
		this.groundPosition = groundPosition;
		this.maxGroundPosition = maxGroundPosition;
		this.maxAltitude = maxAltitude;
		this.tankCapacity = tankCapacity;

		// Initialization of balloon.
		this.fuel = 0;
		this.altitude = 0;
		this.verticalVelocity = 0;
		this.horizontalVelocity = 0;
		this.timeSeconds = 0;

	}

	/// Getters

	/**
	 * Gets the current velocity of the balloon in the vertical direction.
	 * 
	 * @return the vertical velocity.
	 */
	public int getVerticalVelocity() {
		return verticalVelocity;
	}

	/**
	 * Gets the current velocity of the balloon in the horizontal direction.
	 * 
	 * @return the horizontal velocity.
	 */
	public int getHorizontalVelocity() {
		return horizontalVelocity;
	}

	/**
	 * Gets the altitude of the balloon.
	 * 
	 * @return the altitude.
	 */
	public int getAltitude() {
		return altitude;
	}

	/**
	 * Gets the maximum altitude of the balloon. The balloon may not fly above this
	 * value.
	 * 
	 * @return the maximum altitude.
	 */
	public int getMaxAltitude() {
		return maxAltitude;
	}

	/**
	 * Gets the current position of the balloon with respect to the ground (i.e.,
	 * what ground is it flying over).
	 * 
	 * @return the ground position.
	 */
	public int getGroundPosition() {
		return groundPosition;
	}

	/**
	 * Gets the farthest ground position the balloon can travel before wrapping back
	 * around (in a circle) to position 0.
	 * 
	 * @return the maximum ground position.
	 */
	public int getMaxGroundPosition() {
		return maxGroundPosition;
	}

	/**
	 * Gets the amount of fuel the balloon has in its fuel tank.
	 * 
	 * @return the fuel.
	 */
	public double getFuel() {
		return fuel;
	}

	/**
	 * Gets the maximum capacity of the balloon’s fuel tank.
	 * 
	 * @return the tank capacity.
	 */
	public double getTankCapacity() {
		return tankCapacity;
	}

	/**
	 * Gets the maximum rate of fuel burn (assuming there is enough fuel to burn at
	 * this rate).
	 * 
	 * @return the maximum burn rate.
	 */
	public double getMaxBurnRate() {
		return maxBurnRate;
	}

	/// Setters

	/**
	 * Sets the capacity of the fuel tank to the given parameter.
	 * 
	 * @param tankCapacity the new tank capacity.
	 */
	public void setTankCapacity(double tankCapacity) {
		this.tankCapacity = tankCapacity;
	}

	/**
	 * Sets the maximum burn rate to the given parameter.
	 * 
	 * @param maxBurnRate the new maximum burn rate.
	 */
	public void setMaxBurnRate(double maxBurnRate) {
		this.maxBurnRate = maxBurnRate;
	}

	/**
	 * Sets the total time the simulation has run in seconds to the given parameter.
	 * 
	 * @param time the new time in seconds.
	 */
	public void setTime(int time) {
		this.timeSeconds = time;
	}

	/**
	 * Set the ground position back to where it was set by the constructor.
	 */
	public void restoreInitialGroundPosition() {
		this.groundPosition = 0;
	}

	/**
	 * Gets the number of second past the current minute. The returned value must be
	 * between 0 and 60. For example, if the current time is 100, the seconds past
	 * the current minute are 40.
	 * 
	 * @return the seconds component.
	 */
	public int getSeconds() {
		return timeSeconds % 60;
	}

	/**
	 * Gets the number of full minutes past the current hour. The returned value
	 * must be between 0 and 60. For example, if the current time is 100, the
	 * minutes past the current hour is 1.
	 * 
	 * @return the minutes component.
	 */
	public int getMinutes() {
		return (timeSeconds / 60) % 60;
	}

	/**
	 * Gets the number of full hours that have passed. For example, if the current
	 * time is 10,000, the number of full hours is 2.
	 * 
	 * @return the hours component.
	 */
	public int getHours() {
		return timeSeconds / 3600;
	}

	/**
	 * Change the vertical velocity by the given delta (i.e., simply add delta to
	 * the current velocity).
	 * 
	 * @param delta the amount to change the vertical velocity.
	 */
	public void adjustVerticalVelocity(int delta) {
		verticalVelocity += delta;

	}

	/**
	 * Change the horizontal velocity by the given delta (i.e., simply add delta to
	 * the current velocity).
	 * 
	 * @param delta the amount to change the horizontal velocity.
	 */
	public void adjustHorizontalVelocity(int delta) {
		horizontalVelocity += delta;
	}

	/**
	 * Add the given amount of fuel to the tank, however the tank cannot fill past
	 * its maximum capacity. Return the amount of fuel actually added (may be less
	 * than the requested amount if the tank capacity has been reached).
	 * 
	 * @param amount the amount of fuel to add.
	 * @return the actual amount of fuel added.
	 */
	public double addFuel(double amount) {
		double addedFuel = Math.min(amount, tankCapacity - fuel);
		fuel += addedFuel;
		return addedFuel;
	}

	/**
	 * Simulate the balloon's state after a second. Updates burning fuel, ground
	 * position based on horizontal velocity + wrap around behavior, incrementing
	 * the time by one second, and altitude.
	 * 
	 * @return the amount of fuel burned during this update.
	 */
	public double oneSecondUpdate() {
		// Determine the fuel to burn this second: it chooses the smaller value between fuel or maxburnrate.
		double fuelBurn = Math.min(fuel, maxBurnRate);
		fuel -= fuelBurn;
		
		// Updates the and adds the altitude with the vertical velocity, while clamping the result
		// between 0 and maxAltitude.
		altitude = Math.max(0, Math.min(maxAltitude, altitude + verticalVelocity));

		// Updates the ground position by adding groundPosition to horizontalVelocity and repeatedly 
		// using modulos while adding maxGroundPosition to said result.
		groundPosition = ((groundPosition + horizontalVelocity) % maxGroundPosition + maxGroundPosition)
				% maxGroundPosition;
		
		// Increment the simulation time by 1 second.
		timeSeconds = timeSeconds + 1;

		return fuelBurn;
	}

}
