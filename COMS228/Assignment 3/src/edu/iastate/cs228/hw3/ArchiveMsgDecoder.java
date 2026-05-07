package edu.iastate.cs228.hw3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Decodes archived messages compressed using a binary tree encoding scheme.
 * 
 * @author Alexander Tran
 */
public class ArchiveMsgDecoder {

	/**
	 * Main entry point for the message decoder program. Prompts user for a
	 * filename, decodes the message, and prints results.
	 * 
	 * @param args Command-line arguments (not used)
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Please enter filename to decode: ");
		String filename = scanner.nextLine().trim();
		scanner.close();

		try {
			List<String> lines = Files.readAllLines(Paths.get(filename));
			int numLines = lines.size();
			if (numLines < 2) {
				System.err.println("Invalid file format: at least two lines required.");
				return;
			}

			// Properly join tree lines with newline characters.
			String treeString = String.join("\n", lines.subList(0, numLines - 1));
			String message = lines.get(numLines - 1);

			// Reset static index before tree construction.
			MsgTree.staticCharIdx = 0;
			MsgTree root = new MsgTree(treeString);

			System.out.println("character code");
			System.out.println("-------------------------");
			MsgTree.printCodes(root, "");
			System.out.println("");
			System.out.println("MESSAGE:");
			String decodedMessage = root.decode(root, message);

			// Calculate and print statistics.
			printStatistics(decodedMessage, MsgTree.charToCode);
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
		}
	}

	/**
	 * Calculates and prints compression statistics for the decoded message.
	 * 
	 * @param decodedMessage The decoded text message
	 * @param charToCode     Mapping of characters to their binary codes
	 */
	private static void printStatistics(String decodedMessage, Map<Character, String> charToCode) {
		int totalChars = decodedMessage.length();
		if (totalChars == 0) {
			System.out.println("STATISTICS: No characters decoded");
			return;
		}

		int compressedBits = 0;
		for (int i = 0; i < decodedMessage.length(); i++) {
			char c = decodedMessage.charAt(i);
			String code = charToCode.get(c);
			if (code != null) {
				compressedBits += code.length();
			} else {
				// If character not found in tree, use 16 bits as fallback
				compressedBits += 16;
			}
		}

		double uncompressedBits = totalChars * 16.0;
		double avgBitsPerChar = (double) compressedBits / totalChars;
		double spaceSavings = (1.0 - (compressedBits / uncompressedBits)) * 100.0;
		System.out.println("");
		System.out.println("STATISTICS:");
		System.out.printf("Avg bits/char:       %.1f%n", avgBitsPerChar);
		System.out.printf("Total characters:    %d%n", totalChars);
		System.out.printf("Space savings:       %.1f%%%n", spaceSavings);
	}
}

/**
 * Represents a binary tree for message decoding.
 * 
 * @author Your Name
 */
class MsgTree {
	public char payloadChar;
	public MsgTree left;
	public MsgTree right;

	/*
	 * Can use a static char idx to the tree string for recursive solution, but it
	 * is not strictly necessary
	 */
	public static int staticCharIdx = 0;

	/** Mapping of characters to their binary codes */
	public static Map<Character, String> charToCode = new HashMap<>();

	/**
	 * Constructs a message tree from a preorder traversal string.
	 * 
	 * @param encodingString Tree representation using '^' for internal nodes
	 */
	public MsgTree(String encodingString) {
		if (staticCharIdx >= encodingString.length()) {
			return;
		}
		payloadChar = encodingString.charAt(staticCharIdx++);
		if (payloadChar == '^') {
			left = new MsgTree(encodingString);
			right = new MsgTree(encodingString);
		} else {
			left = null;
			right = null;
		}
	}

	/**
	 * Recursively prints character codes in preorder traversal.
	 * 
	 * @param root Root node of the tree
	 * @param code Current binary path code (start with empty string)
	 */
	public static void printCodes(MsgTree root, String code) {
		if (root == null) {
			return;
		}
		if (root.left == null && root.right == null) {
			String charRep;
			switch (root.payloadChar) {
			case ' ':
				charRep = "space";
				break;
			case '\n':
				charRep = "newline";
				break;
			case '\t':
				charRep = "tab";
				break;
			case '\r':
				charRep = "carriage-return";
				break;
			default:
				charRep = String.valueOf(root.payloadChar);
			}
			System.out.println(charRep + "\t  " + code);
			charToCode.put(root.payloadChar, code);
		}
		if (root.left != null) {
			printCodes(root.left, code + "0");
		}
		if (root.right != null) {
			printCodes(root.right, code + "1");
		}
	}

	/**
	 * Decodes a binary message using the constructed tree.
	 * 
	 * @param root Root node of the decoding tree
	 * @param msg  Binary message string (composed of '0's and '1's)
	 * @return Decoded text message
	 */
	public String decode(MsgTree root, String msg) {
		StringBuilder decodedMsg = new StringBuilder();
		MsgTree current = root;
		for (int i = 0; i < msg.length(); i++) {
			char bit = msg.charAt(i);
			if (bit == '0') {
				current = current.left;
			} else if (bit == '1') {
				current = current.right;
			}
			if (current == null) {
				break;
			}
			if (current.left == null && current.right == null) {
				decodedMsg.append(current.payloadChar);
				current = root;
			}
		}
		System.out.println(decodedMsg.toString());
		return decodedMsg.toString();
	}
}