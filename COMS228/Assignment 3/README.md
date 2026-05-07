# Archived Message Reconstruction

A Java decoding project created for COM S 228.  
This project reconstructs an archived message using a binary-tree-based encoding scheme.

## Project Overview

This project implements a decoder for messages compressed with a binary tree encoding system. The program reads an `.arch` file, reconstructs the character encoding tree, prints the character codes, and decodes the compressed bit string back into the original message.

The encoding works similarly to a prefix-code tree. Each character is stored at a leaf node, and each path from the root to a leaf represents that character's binary code. Moving left represents `0`, and moving right represents `1`.

## Features

- Reads archived message files ending in `.arch`
- Builds a binary message tree from an encoding string
- Supports preorder traversal tree construction
- Handles internal tree nodes and character leaf nodes
- Prints each character and its binary code
- Decodes compressed bit strings into readable messages
- Supports files where the encoding tree may span two lines
- Includes optional message statistics for extra credit

## Technologies Used

- Java
- Binary trees
- Recursion
- File input
- String parsing
- Preorder traversal
- Console input/output
- Javadoc

## Main Class

### `MsgTree`

The `MsgTree` class represents the binary encoding tree used to decode the archived message.

Each node contains:

- A character payload
- A reference to the left child
- A reference to the right child

Internal nodes do not represent characters. Leaf nodes contain the actual decoded characters.

Important members and methods include:

- `payloadChar`
- `left`
- `right`
- `MsgTree(String encodingString)`
- `MsgTree(char payloadChar)`
- `printCodes(MsgTree root, String code)`
- `decode(MsgTree codes, String msg)`

## Encoding Format

The archive file contains either two or three lines:

1. The encoding scheme
2. Possibly another line of the encoding scheme if a newline character is part of the tree
3. The compressed message bit string

The encoding scheme is stored as a preorder traversal of the binary tree.

In the encoding string:

- `^` represents an internal node
- Any other character represents a leaf node containing that character

Example encoding tree string:

```text
^a^^!^dc^rb
```

Example compressed message:

```text
10110101011101101010100
```

This message decodes to:

```text
cadbard!
```

## How Decoding Works

To decode one character:

1. Start at the root of the tree.
2. Read one bit from the compressed message.
3. Move left if the bit is `0`.
4. Move right if the bit is `1`.
5. Continue until a leaf node is reached.
6. Print the character stored in that leaf.
7. Return to the root and decode the next character.

Because the encoding is prefix-based, the program can determine where one character ends and the next begins without needing separators.

## Example Output

```text
character code
-------------------------
a         0
!         100
d         1010
c         1011
r         110
b         111

MESSAGE:
cadbard!
```

## Optional Statistics

The project also supports optional message statistics.

Example:

```text
STATISTICS:
Avg bits/char: 8.0
Total characters: 1180
Space savings: 50.0%
```

The space savings calculation assumes that an uncompressed character uses 16 bits.

## What I Learned

Through this project, I practiced using binary trees to solve a real decoding problem. I learned how tree traversal can represent encoded data and how a recursive constructor can rebuild a tree from a preorder string.

I also gained experience with file parsing, especially when the input format could vary depending on whether the encoding included a newline character. This made the project more challenging than simply reading a fixed two-line file.

## Challenges

One of the biggest challenges was correctly rebuilding the message tree from the encoding string. Since `^` represented internal nodes and all other characters represented leaves, the constructor had to carefully track the current position in the string while building the tree recursively.

Another challenge was decoding the message bit by bit. The program had to reset back to the root every time it reached a leaf node, while still continuing through the compressed message without losing its place.

Handling special characters, such as spaces and newline characters, also required extra care so that the decoded message matched the original text.

## How to Run

1. Place the source files in the package:

```text
edu.iastate.cs228.hw3
```

2. Compile the Java files.

3. Run the main program.

4. When prompted, enter the name of the `.arch` file:

```text
Please enter filename to decode:
```

5. The program will print the character encoding table followed by the decoded message.

## Example Usage

```text
Please enter filename to decode: cadbard.arch

character code
-------------------------
a         0
!         100
d         1010
c         1011
r         110
b         111

MESSAGE:
cadbard!
```

## Author

Alexander Tran
