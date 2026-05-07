package hw2;

/**
 * Model of a Monopoly-like game. Two players take turns rolling dice to move
 * around a board. The game ends when one of the players has at least
 * MONEY_TO_WIN money or one of the players goes bankrupt (they have negative
 * money).
 * 
 * @author Alexander Tran
 */
public class CyGame {
	/**
	 * The endzone square type.
	 */
	public static final int ENDZONE = 0;
	/**
	 * The CyTown square type.
	 */
	public static final int CYTOWN = 1;
	/**
	 * The pay rent square type.
	 */
	public static final int PAY_RENT = 2;
	/**
	 * The fall behind square type.
	 */
	public static final int FALL_BEHIND = 3;
	/**
	 * The blizzard square type.
	 */
	public static final int BLIZZARD = 4;
	/**
	 * The pass class square type.
	 */
	public static final int PASS_CLASS = 5;
	/**
	 * Points awarded when landing on or passing over the endzone square.
	 */
	public static final int ENDZONE_PRIZE = 200;
	/**
	 * The standard rent payed to the other player when landing on a pay rent
	 * square.
	 */
	public static final int STANDARD_RENT_PAYMENT = 80;
	/**
	 * The cost to by CyTown.
	 */
	public static final int CYTOWN_COST = 200;
	/**
	 * The amount of money required to win.
	 */
	public static final int MONEY_TO_WIN = 400;

	/*
	 * Private variables used to keep track of player money, starting money, player
	 * position and more.
	 */
	private int currentPlayer;
	private int player1Square;
	private int player2Square;
	private int player1Property;
	private int player2Property;
	private int player1Money;
	private int player2Money;
	private int totalSquares;
	private int startingMoney;
	private boolean isGameOver;
	private int rentAmount;

	/**
	 * Constructor to Initialize game.
	 * 
	 * @param numSquares number of squares in game.
	 * @param startingMoney money to start game with.
	 */
	public CyGame(int numSquares, int startingMoney) {
		currentPlayer = 1;
		this.totalSquares = numSquares;
		this.startingMoney = startingMoney;
		player1Money = this.startingMoney;
		player2Money = this.startingMoney;
		player1Square = 0;
		player2Square = 0;
		player1Property = 1;
		player2Property = 1;
		isGameOver = false;

	}

	/**
	 * Method to buy CyTown. This also functions as a way to check if the player HAS
	 * enough money to buy CyTown, IF it is buyable (cannot be bought by a player if
	 * it is already owned by the other player.) it will also check which player is
	 * currently able to buy, and updates their property accordingly.
	 */
	public void buyCyTown() {
		if (!isGameEnded()) {
			// If CyTown is already owned, prevent another purchase
			if (isPlayer1CyTownOwner() || isPlayer2CyTownOwner()) {
				return;
			}

			if (getCurrentPlayer() == 1) {
				if (getSquareType(player1Square) == CYTOWN && player1Money >= CYTOWN_COST) {
					player1Money -= CYTOWN_COST;
					player1Property = 2; // Set ownership correctly
					endTurn();
				}
			} else if (getCurrentPlayer() == 2) {
				if (getSquareType(player2Square) == CYTOWN && player2Money >= CYTOWN_COST) {
					player2Money -= CYTOWN_COST;
					player2Property = 2; // Set ownership correctly
					endTurn();
				}
			}
		}
	}

	/**
	 * This method ends the turn for the player currently playing, IF the
	 * currentPlayer is 1 It will set it to currentPlayer == 2. Else, it just stays
	 * at 1.
	 */
	public void endTurn() {
		currentPlayer = (currentPlayer == 1) ? 2 : 1;
	}

	/**
	 * A method that returns who is currently playing.
	 * 
	 * @return the player currently playing during a turn.
	 */
	public int getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * A method that returns the player who is not currently playing, the other
	 * player.
	 * 
	 * @return the player who is not the current player.
	 */
	public int getOtherPlayer() {
		return (currentPlayer == 1) ? 2 : 1;
	}

	/**
	 * Get the given(based off of int player) player's money.
	 * 
	 * @param player to be selected to get money.
	 * @return the given player's money.
	 */
	public int getPlayerMoney(int player) {
		return (player == 1) ? player1Money : player2Money;
	}

	/**
	 * Get the given player's square location.
	 * 
	 * @param player to be selected for position.
	 * @return the given player's square location.
	 */
	public int getPlayerSquare(int player) {
		if (player == 1) {
			return player1Square;
		} else {
			return player2Square;
		}
	}

	/**
	 * Get the type of square code (see constants defined at the top of this class)
	 * for the given square location. Each square is assigned a single type based on
	 * the following rules. The rules are listed in order of highest to lowest
	 * precedence (i.e., when multiple rules match a square number, apply the one
	 * higher on the list.
	 * 
	 * @param square type to be selected.
	 * @return the code (as defined by in the constants) for the square type
	 */
	public int getSquareType(int square) {

		if (square == 0) {
			return ENDZONE;
		} else if ((square % totalSquares) == totalSquares - 1) {
			return CYTOWN;
		} else if (square % 5 == 0) {
			return PAY_RENT;
		} else if (square % 7 == 0 || square % 11 == 0) {
			return FALL_BEHIND;
		} else if (square % 3 == 0) {
			return BLIZZARD;
		} else {
			return PASS_CLASS;
		}

	}

	/**
	 * Returns true if game is over, false otherwise. The game is over when either
	 * player has at least MONEY_TO_WIN money or either player has a negative amount
	 * of money.
	 * 
	 * @return true if the game is over, false otherwise
	 */
	public boolean isGameEnded() {
		if (player1Money >= MONEY_TO_WIN || player2Money >= MONEY_TO_WIN || player1Money < 0 || player2Money < 0) {
			isGameOver = true;
		}
		return isGameOver;
	}

	/**
	 * Returns true if player 1 owns CyTown, false otherwise.
	 * 
	 * @return if player 1 owns CyTown
	 */
	public boolean isPlayer1CyTownOwner() {
		if (player1Property >= 2) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if player 2 owns CyTown, false otherwise.
	 * 
	 * @return if player 2 owns CyTown
	 */
	public boolean isPlayer2CyTownOwner() {
		if (player2Property >= 2) {
			return true;
		}
		return false;
	}

	/**
	 * This method is called to indicate the die has been rolled. Advance the
	 * current player forward by the same number of squares as the roll. EXCEPTION:
	 * If the player is currently on a BLIZZARD square they only move forward if the
	 * value rolled is odd, otherwise their turn is over. If the player passes over
	 * the ENDZONE square add the ENDZONE_PRIZE to the player's money. Then apply
	 * the action of the square the player lands on. A player can only collect the
	 * ENDZONE_PRIZE once per turn (i.e., passing the endzone multiple times in a
	 * turn doesn't result in extra payouts).
	 * 
	 * Actions below:
	 * 
	 * ENDZONE: When a player lands on the endzone (or passes over the endzone, as
	 * described above) award them with ENDZONE_PRIZE money.
	 * 
	 * CYTOWN: The CyTown square is a property. If unowned, gives the player the
	 * option to buy it or pass.
	 * 
	 * PAY_RENT: When a player lands on the pay rent square they must pay the other
	 * player. Doubled if the other player owns CyTown.
	 * 
	 * FALL_BEHIND: The player goes backward one square and performs whatever action
	 * that square requires.
	 * 
	 * BLIZZARD: No additional action is taken for the remainder of the players
	 * turn. In future turns, the player stays stuck on the blizzard square until
	 * they roll an odd number.
	 * 
	 * PASS_CLASS: The player moves forward an additional 4 squares and performs
	 * whatever action that square requires.
	 * 
	 * METHOD WILL NOT DO ANYTHING IF GAME IS ALREADY ENDED.
	 * 
	 * @param value of die roll.
	 */
	public void roll(int value) {
		if (isGameEnded())
			return;

		if (currentPlayer == 1) {
			if (getSquareType(player1Square) != BLIZZARD || value % 2 == 1) {
				if ((player1Square + value) >= totalSquares) {
					player1Money += ENDZONE_PRIZE;
				}
				player1Square = (player1Square + value) % totalSquares;
			}

			if (getSquareType(player1Square) == FALL_BEHIND) {
				player1Square = (player1Square - 1 + totalSquares) % totalSquares;
			}
			if (getSquareType(player1Square) == PAY_RENT) {
				rentAmount = isPlayer2CyTownOwner() ? (2 * STANDARD_RENT_PAYMENT) : STANDARD_RENT_PAYMENT;
				player1Money -= rentAmount;
				player2Money += rentAmount;
			}

			if (getSquareType(player1Square) == PASS_CLASS) {
				if ((player1Square + 4) >= totalSquares) {
					player1Money += ENDZONE_PRIZE;
				}
				player1Square = (player1Square + 4) % totalSquares;
			}
			if (getSquareType(player1Square) == CYTOWN) {
				return; // Stop turn from ending to allow buy
			}
		} else { // Player 2 logic
			if (getSquareType(player2Square) != BLIZZARD || value % 2 == 1) {
				if ((player2Square + value) >= totalSquares) {
					player2Money += ENDZONE_PRIZE;
				}
				player2Square = (player2Square + value) % totalSquares;
			}

			if (getSquareType(player2Square) == FALL_BEHIND) {
				player2Square = (player2Square - 1 + totalSquares) % totalSquares;
			}
			if (getSquareType(player2Square) == PAY_RENT) {
				rentAmount = isPlayer1CyTownOwner() ? (2 * STANDARD_RENT_PAYMENT) : STANDARD_RENT_PAYMENT;
				player2Money -= rentAmount;
				player1Money += rentAmount;
			}

			if (getSquareType(player2Square) == PASS_CLASS) {
				if ((player2Square + 4) >= totalSquares) {
					player2Money += ENDZONE_PRIZE;
				}
				player2Square = (player2Square + 4) % totalSquares;
			}
			if (getSquareType(player2Square) == CYTOWN) {
				return; // Stop turn from ending to allow buy
			}
		}
		endTurn();
	}

	/**
	 * Returns a one-line string representation of the current game state. The
	 * format is:
	 * <p>
	 * <tt>Player 1*: (0, false, $0) Player 2: (0, false, $0)</tt>
	 * <p>
	 * The asterisks next to the player's name indicates which players turn it is.
	 * The values (0, false, $0) indicate which square the player is on, if the
	 * player is the owner of CyTown, and how much money the player has
	 * respectively.
	 * 
	 * @return one-line string representation of the game state
	 */
	public String toString() {
		String fmt = "Player 1%s: (%d, %b, $%d) Player 2%s: (%d, %b, $%d)";
		String player1Turn = "";
		String player2Turn = "";
		if (getCurrentPlayer() == 1) {
			player1Turn = "*";
		} else {
			player2Turn = "*";
		}
		return String.format(fmt, player1Turn, getPlayerSquare(1), isPlayer1CyTownOwner(), getPlayerMoney(1),
				player2Turn, getPlayerSquare(2), isPlayer2CyTownOwner(), getPlayerMoney(2));
	}
}