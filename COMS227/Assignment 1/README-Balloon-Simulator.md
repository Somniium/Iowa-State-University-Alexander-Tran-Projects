# Balloon Simulator

A Java-based hot air balloon simulation project created for COM S 227.  
The project models the movement of a hot air balloon over time by tracking altitude, ground position, velocity, fuel usage, burn rate, and simulation time.

## Project Overview

This project implements a `Balloon` class that represents a hot air balloon in a simplified flight simulation. The balloon can move vertically and horizontally, burn fuel each second, wrap around a circular ground path, and keep track of elapsed time in hours, minutes, and seconds.

The goal of the project was to practice object-oriented programming, instance variables, accessor and mutator methods, arithmetic expressions, modular arithmetic, and Java documentation.

## Features

- Tracks vertical and horizontal velocity
- Updates balloon altitude based on vertical velocity
- Prevents the balloon from flying above its maximum altitude
- Tracks horizontal ground position
- Wraps ground position around a circular field using modular arithmetic
- Tracks available fuel and fuel tank capacity
- Burns fuel based on a maximum burn rate
- Allows fuel to be added without exceeding tank capacity
- Tracks simulation time in seconds, minutes, and hours
- Restores the balloon to its initial ground position
- Uses accessor and mutator methods to manage object state

## Technologies Used

- Java
- Eclipse IDE
- Javadoc
- Gradescope/autograder testing

## Main Class

### `Balloon`

The `Balloon` class stores the state of the balloon and provides methods for updating and retrieving simulation information.

Important methods include:

- `addFuel(double amount)`
- `oneSecondUpdate()`
- `adjustVerticalVelocity(int delta)`
- `adjustHorizontalVelocity(int delta)`
- `setMaxBurnRate(double maxBurnRate)`
- `setTankCapacity(double tankCapacity)`
- `setTime(int time)`
- `restoreInitialGroundPosition()`
- `getAltitude()`
- `getGroundPosition()`
- `getFuel()`
- `getHours()`
- `getMinutes()`
- `getSeconds()`

## Example Usage

```java
Balloon b = new Balloon(37, 40, 200, 20);

b.addFuel(10);
b.setMaxBurnRate(2);
b.adjustVerticalVelocity(5);
b.adjustHorizontalVelocity(3);

double burned = b.oneSecondUpdate();

System.out.println("Fuel burned: " + burned);
System.out.println("Altitude: " + b.getAltitude());
System.out.println("Ground position: " + b.getGroundPosition());
```

## What I Learned

Through this project, I practiced building a Java class directly from a specification. I learned how to choose appropriate instance variables, separate accessor methods from mutator methods, and update object state in a controlled way.

I also gained experience with modular arithmetic, especially for wrapping the balloon's ground position around a circular field. Another important part of the project was learning how to enforce limits, such as maximum altitude and fuel tank capacity, using mathematical expressions.

## Challenges

One of the main challenges was handling position wrapping correctly when the balloon moved past the edge of the ground field, especially when the horizontal velocity was negative. Another challenge was keeping the class simple by only storing permanent object state as instance variables and using local variables for temporary calculations.

## How to Run

This project does not include a full application with a `main` method by default. The `Balloon` class is meant to be tested using a separate tester class.

To run your own tests:

1. Create a Java project.
2. Place `Balloon.java` inside the `hw1` package.
3. Create a separate test class with a `main` method.
4. Instantiate a `Balloon` object and call its methods.

Example:

```java
public class SimpleTests {
    public static void main(String[] args) {
        Balloon b = new Balloon(37, 40, 200, 20);

        System.out.println("Initial fuel: " + b.getFuel());
        b.addFuel(10);
        System.out.println("Fuel after adding: " + b.getFuel());

        b.adjustVerticalVelocity(4);
        b.oneSecondUpdate();
        System.out.println("Altitude after update: " + b.getAltitude());
    }
}
```

## Author

Alexander Tran
