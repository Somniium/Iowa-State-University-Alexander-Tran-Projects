# Boulder Slider

A Java sliding puzzle game backend created for COM S 227.  
This project implements the core logic for a grid-based boulder puzzle where the player moves horizontal and vertical boulders to reach an exit.

## Project Overview

Boulder Slider is a Java project that models the backend logic for a sliding boulder puzzle game. The game is played on a 2D grid made of walls, ground cells, and an exit. Boulders can occupy multiple cells and are either horizontal or vertical.

The objective is to move one of the boulders to the exit. The project includes logic for parsing board layouts, tracking boulder positions, validating legal moves, moving boulders, resetting the board, and storing move history.

A Java Swing GUI was provided with the starter code, but the main focus of this project was implementing the backend classes that power the game.

## Features

- Represents a 2D board using cells
- Supports ground, wall, and exit cell types
- Models horizontal and vertical boulders
- Allows boulders to move only in their valid orientation
- Tracks which boulder is currently grabbed by the user
- Moves grabbed boulders one cell at a time
- Prevents illegal movement through walls or other boulders
- Updates both the board cells and boulder positions after each move
- Tracks move count and move history
- Supports resetting the board to its original state
- Finds all possible legal moves from the current board state
- Includes optional extra-credit solver support

## Technologies Used

- Java
- Java Swing
- Eclipse IDE
- Javadoc
- Gradescope/Specchecker testing

## Main Classes

### `Boulder`

Represents one boulder on the board. A boulder stores its starting position, current position, length, and orientation.

Main responsibilities:

- Track a boulder's row and column
- Track the original starting location
- Move in the correct direction based on orientation
- Reset back to the original location

### `Board`

Represents the full puzzle board and manages the game state.

Main responsibilities:

- Store the 2D grid of cells
- Store the list of boulders
- Track the currently grabbed boulder
- Move boulders when legal
- Update move history
- Check whether the puzzle has been solved
- Reset the board
- Find all possible legal moves

### `GridUtil`

Contains static helper methods for converting a text-based board description into usable game objects.

Main responsibilities:

- Create the grid of cells from symbols
- Find and create boulders from a board description
- Interpret symbols such as walls, exits, ground, and boulder segments

### `Solver`

An optional extra-credit class used to recursively search for solutions to a board setup.

## Board Symbols

The board descriptions use symbols to represent different parts of the puzzle:

- `*` = wall
- `e` = exit
- `.` = ground
- `[ ]` = horizontal boulder of length 2
- `[ # # ]` = longer horizontal boulder
- `^` and `v` = top and bottom of a vertical boulder
- `#` = middle segment of a longer boulder

## Example Board

```text
* * * * *
* . . . *
* [ ] . e
* . . . *
* * * * *
```

## Example Usage

```java
Cell[][] cells = GridUtil.createGrid(testDescription);
ArrayList<Boulder> boulders = GridUtil.findBoulders(testDescription);

Board board = new Board(cells, boulders);

board.grabBoulderAt(2, 1);
board.moveGrabbedBoulder(Direction.RIGHT);
board.releaseBoulder();

System.out.println(board);
```

## What I Learned

Through this project, I practiced working with multiple interacting Java classes instead of only one standalone class. I gained experience using arrays, `ArrayList`, loops, enums, helper methods, and object references.

I also learned how important it is to keep object state consistent. When a boulder moves, both the boulder object and the cells on the board have to be updated correctly so the board remains accurate.

## Challenges

The hardest part of this project was managing movement correctly. A move was only valid if the boulder was moving in the correct direction, stayed inside the board, did not hit a wall, and did not collide with another boulder.

Another challenge was implementing reset and undo-style behavior, because the board needed to restore the boulders, cells, move count, move history, and game-over status back to a previous or starting state.

## How to Run

This project can be tested through simple console test classes or through the provided Java Swing GUI.

To run with your own tests:

1. Import the starter project into Eclipse.
2. Complete the classes inside the `hw3` package.
3. Create or run a test class with a `main` method.
4. Instantiate `Board`, `Boulder`, and `GridUtil` objects.
5. Print the board or inspect object state after each move.

To run the GUI:

1. Open the project in Eclipse.
2. Run `ui.GameMain`.
3. Load a puzzle board.
4. Click and drag boulders to move them.

## Author

Alexander Tran
