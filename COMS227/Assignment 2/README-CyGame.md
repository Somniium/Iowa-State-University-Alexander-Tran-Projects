# CyGame

A Java-based board game simulation created for COM S 227.  
This project models a two-player Monopoly-inspired game where players move around a circular board, earn money, pay rent, buy property, and try to win before going bankrupt.

## Project Overview

`CyGame` is a single-class Java project that manages the full state of a turn-based board game. The game has two players, each with a position on the board, a money balance, and the ability to move by rolling a die.

The board is made up of numbered squares arranged in a circular pattern. Each square has a specific type, and the type of square a player lands on determines what happens during that turn.

This project was mainly focused on practicing conditional logic, object state, helper methods, and writing code directly from a specification.

## Game Rules

The board contains several types of squares:

- **Endzone**: Awards prize money when a player lands on or passes over it.
- **CyTown**: A purchasable property square.
- **Pay Rent**: Makes the current player pay money to the other player.
- **Fall Behind**: Moves the current player backward one square.
- **Blizzard**: Can trap a player until they roll an odd number.
- **Pass Class**: Moves the current player forward an additional four squares.

The game ends when one player reaches the required winning amount of money or when one player becomes bankrupt.

## Features

- Tracks two players and their current board positions
- Tracks each player's money balance
- Handles turn switching between players
- Determines square types based on board position
- Supports circular board movement
- Awards money for passing or landing on the Endzone
- Allows a player to buy CyTown
- Applies rent rules, including increased rent when CyTown is owned
- Handles Blizzard movement restrictions
- Handles Fall Behind and Pass Class square behavior
- Detects when the game is over

## Technologies Used

- Java
- Eclipse IDE
- Javadoc
- Gradescope/Specchecker testing

## Main Class

### `CyGame`

The `CyGame` class stores and updates the game state. It tracks player positions, player money, current turn, CyTown ownership, board size, and game-over conditions.

Important responsibilities include:

- Initializing a new game
- Returning the current player
- Determining the type of a board square
- Moving players around the board
- Applying square effects after movement
- Managing money transfers between players
- Handling property ownership
- Checking whether the game has ended

## Example Usage

```java
CyGame game = new CyGame(16, 200);

System.out.println(game);

System.out.println("Square 0 type: " + game.getSquareType(0));
System.out.println("Square 5 type: " + game.getSquareType(5));
System.out.println("Square 15 type: " + game.getSquareType(15));
```

## What I Learned

Through this project, I practiced designing a Java class that keeps track of several pieces of internal state at the same time. I learned how to use conditionals to model different game rules and how to make sure that each player’s position, money, and turn status stayed consistent after every move.

I also gained more experience with circular board movement, rule precedence, and testing small parts of a class before moving on to more complicated behavior.

## Challenges

One of the biggest challenges was making sure square actions happened in the correct order. Some squares could trigger additional movement, such as Pass Class or Fall Behind, which meant the game sometimes needed to apply more than one rule during a single turn.

Another challenge was handling the Endzone correctly. A player could receive prize money for passing or landing on it, but only once per turn.

## How to Run

This project does not include a full application with a built-in `main` method. The `CyGame` class is meant to be tested using a separate tester class.

To run your own tests:

1. Create a Java project.
2. Place `CyGame.java` inside the `hw2` package.
3. Create a separate test class with a `main` method.
4. Instantiate a `CyGame` object and call its methods.

Example:

```java
public class CyGameTest {
    public static void main(String[] args) {
        CyGame game = new CyGame(16, 200);

        System.out.println("Starting game:");
        System.out.println(game);

        System.out.println("Board square types:");
        for (int i = 0; i < 16; i++) {
            System.out.println("Square " + i + ": " + game.getSquareType(i));
        }
    }
}
```

## Author

Alexander Tran
