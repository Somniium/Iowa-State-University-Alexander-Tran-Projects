# Model Train Link Simulation

A Java model train track simulation project created for COM S 227.  
This project implements several types of track links that connect paths and allow a train to move between different track segments.

## Project Overview

This project models part of a train simulation system. The train moves along paths made of points, and links connect two or more path endpoints. When a train reaches a link, the link determines which connected path the train should move onto next.

The project focuses on interfaces, inheritance, abstract classes, composition, and reducing duplicated code through a clean class hierarchy.

## Features

- Models links between train track paths
- Supports path endpoints connected through shared coordinates
- Moves a train's `PositionVector` from one path to another
- Implements the `Crossable` and `Traversable` behaviors
- Supports fixed links, switchable links, turning links, and dead ends
- Tracks when a train enters or exits a crossing
- Uses inheritance to share common link behavior
- Uses composition to manage connected point pairs
- Includes support for a simulation GUI through the provided starter code

## Technologies Used

- Java
- Java interfaces
- Abstract classes
- Inheritance and polymorphism
- Composition
- Eclipse IDE
- Javadoc
- Gradescope testing

## Main Interfaces

### `Traversable`

Defines behavior for objects that can move a train from one pair of points to the next.

Main method:

- `shiftPoints(PositionVector positionVector)`

### `Crossable`

Extends `Traversable` and defines behavior for links that can be crossed by a train.

Main methods:

- `getConnectedPoint(Point point)`
- `trainEnteredCrossing()`
- `trainExitedCrossing()`

## Main Classes

### `AbstractLink`

An abstract base class used to share common link behavior across multiple concrete link types. This class helps reduce duplicate code and provides a cleaner inheritance structure.

### `DeadEndLink`

Represents a link where a path ends and does not connect to another path.

### `CouplingLink`

Connects two path endpoints together. When the train reaches one endpoint, it can continue onto the connected endpoint.

### `StraightLink`

Represents a three-path link where the train continues straight through the crossing.

### `TurnLink`

Represents a three-path link where the train takes the turning connection instead of the straight connection.

### `SwitchLink`

Represents a three-path link that can switch between straight and turning behavior.

### `MultiFixedLink`

Represents a link made from multiple fixed point-pair connections.

### `MultiSwitchLink`

Represents a multi-path link where connections can be changed dynamically.

## Simulation Concepts

The project uses several provided API classes:

- `Point`: Represents a coordinate on a path.
- `Path`: Represents a sequence of points.
- `PositionVector`: Represents the train's current direction and position between two points.
- `PointPair`: Represents a connection between two points.
- `Track`: Represents a collection of paths in the simulation.

## Example Usage

```java
Track track = new Track();

Path path1 = track.addPathType(PathTypes.westToEast, 5, 5);
Path path2 = track.addPathType(PathTypes.westToEast, 6, 5);

Crossable link = new CouplingLink(path1.getHighpoint(), path2.getLowpoint());

path1.setHighEndpointLink(link);
path2.setLowEndpointLink(link);

PositionVector position = new PositionVector();
position.setPointA(path1.getPointByIndex(path1.getHighpoint().getPointIndex() - 1));
position.setPointB(path1.getHighpoint());

link.shiftPoints(position);
```

## What I Learned

Through this project, I practiced designing a class hierarchy instead of solving everything with repeated code. I learned how abstract classes can store shared behavior while still allowing subclasses to define their own specific link behavior.

I also gained more experience with interfaces and polymorphism. Since all link types implement the same crossing behavior, the simulation can treat different link classes through the same `Crossable` interface.

## Challenges

One of the main challenges was deciding what code belonged in the abstract superclass and what code belonged in each specific subclass. The goal was to avoid repeating the same logic across link classes while still keeping the behavior of each link clear.

Another challenge was correctly updating the `PositionVector` when the train crossed from one path to another. The direction of travel had to stay consistent as the train moved across different connected endpoints.

## How to Run

This project can be tested with simple console tests or with the provided simulation GUI.

To run with tests:

1. Import the starter project into Eclipse.
2. Complete the classes inside the `hw4` package.
3. Create a test class with a `main` method.
4. Build tracks, paths, links, and position vectors.
5. Call link methods and verify the resulting connected points or position vectors.

To run the simulation GUI:

1. Open the project in Eclipse.
2. Run the provided `ui.SimMain` class.
3. Use the visualization to observe how trains move through paths and links.

## Author

Alexander Tran
