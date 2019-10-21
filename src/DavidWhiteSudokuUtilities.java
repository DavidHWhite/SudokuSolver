import java.util.Scanner;

/*
 * Program: DavidWhiteSudokuUtilities
 *
 * Author: David White
 *
 * Date: 5/29/2019
 *
 * Version: 1.0
 *
 * Description: A program which generates and solves 9x9 sudoku puzzles.
 */
public class DavidWhiteSudokuUtilities {
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);

		System.out.println("**********************************************************");
		System.out.println("*********** ---- David's Sudoku Utilities ---- ***********");
		System.out.println("**********************************************************\n");
		System.out.println("     Thank you for choosing David's Sudoku Utilities!\n");
		// Loop until the user says to stop
		boolean mainLooping = true;
		while (mainLooping) {
			// Prompt the user for what they'd like to do
			boolean promptLooping = true;
			while (promptLooping) {
				// Prompt the user to ask if the program should repeat
				System.out.print("Would you like to solve or generate a puzzle? (generate/solve) ");
				// Get the user's response and interpret it
				String response = input.nextLine();
				System.out.println();
				if (response.length() > 0 && response.substring(0, 1).equalsIgnoreCase("g")) {
					// If they respond "generate", stop looping and run generatePuzzle()
					promptLooping = false;
					generatePuzzle(input);
				} else if (response.length() > 0 && response.substring(0, 1).equalsIgnoreCase("s")) {
					// If they respond "solve", stop looping and run solvePuzzle()
					promptLooping = false;
					solvePuzzle(input);
				} else {
					// If they respond in some other way, ask them to respond again
					System.out.println("I'm sorry, I didn't quite catch that.");
				}
			}

			// Check if the user would like to repeat again
			promptLooping = true;
			while (promptLooping) {
				// Prompt the user to ask if the program should repeat
				System.out.print("Would you like to do anything else? (y/n) ");
				// Get the user's response and interpret it
				String response = input.nextLine();
				if (response.equalsIgnoreCase("y")) {
					// If they respond "y", continue looping after printing a blank line for spacing
					promptLooping = false;
				} else if (response.equalsIgnoreCase("n")) {
					// If they respond "n", stop looping
					promptLooping = false;
					mainLooping = false;
					System.out.println("Quitting...");
				} else {
					// If they respond in some other way, ask them to respond again
					System.out.println("I'm sorry, I didn't quite catch that.");
				}
			}
		}
	}

	/**
	 * Static helper method which runs the generation of a puzzle
	 */
	private static void generatePuzzle(Scanner input) {
		// Generate puzzle
		SudokuGenerator generator = new SudokuGenerator();

		// Print unsolved puzzle
		System.out.println("Here's your unsolved puzzle: ");
		printPuzzle(generator.getUnsolvedPuzzle());

		// Wait to print the solved puzzle until requested
		System.out.println("Press enter when you're ready to see the solution.");
		input.nextLine();
		System.out.println("Here's the solved puzzle:");
		printPuzzle(generator.getSolvedPuzzle());
	}

	/**
	 * Static helper method which runs the solving of a puzzle
	 */
	private static void solvePuzzle(Scanner input) {
		// Create puzzle int array
		int[][] puzzle = null;

		// Loop until a valid puzzle is submitted
		boolean haveValidInput = false;
		while (!haveValidInput) {
			// Prompt the user for input
			System.out.println("Please input a sudoku puzzle to solve.\n" +
				                   "Type the puzzle, separating each row with a line break. Type '0' for an empty cell.\n" +
				                   "For example, if I wanted to type a row that was empty except for a 9 in the third cell, I would type '009000000'.");
			puzzle = new int[9][9];
			for (int i = 0; i < 9; i++) {
				// Get user input
				String line = input.nextLine();

				// Validate input
				boolean needsRefresh = false;
				try {
					if (line.length() != 9) {
						System.out.println("I'm sorry, but you don't seen to have entered 9 digits.\nRemember, you need to type one entire line at a time!");
						needsRefresh = true;
					}
				} catch (NumberFormatException e) {
					System.out.println("I'm sorry, but please be sure to only enter numeric characters.\nRemember, use a 0 for an empty cell.");
					needsRefresh = true;
				}
				// If input is valid
				if (needsRefresh) {
					System.out.println("Please enter that line again." + (i > 0 ? " Here's everything else you've typed so far:" : ""));
					for (int j = 0; j < i; j++) {
						for (int k = 0; k < 9; k++)
							System.out.print(puzzle[j][k]);
						System.out.println();
					}
					i--;
				} else for (int j = 0; j < 9; j++) puzzle[i][j] = Integer.parseInt(line.substring(j, j + 1));
			}

			// Validate the puzzle input
			boolean[] presInRow, presInCol, presInBox;
			haveValidInput = true;
			validate:
			for (int i = 0; i < 9; i++) {
				presInRow = new boolean[] {false, false, false, false, false, false, false, false, false, false};
				presInCol = new boolean[] {false, false, false, false, false, false, false, false, false, false};
				presInBox = new boolean[] {false, false, false, false, false, false, false, false, false, false};
				for (int j = 0; j < 9; j++) {
					int boxRow = 3 * (i / 3) + (j / 3), boxCol = 3 * (i % 3) + (j % 3);
					if ((puzzle[i][j] != 0 && presInRow[puzzle[i][j]])
						|| (puzzle[j][i] != 0 && presInCol[puzzle[j][i]])
						|| (puzzle[boxRow][boxCol] != 0 && presInBox[puzzle[boxRow][boxCol]])) {
						System.out.println("This doesn't seem to be a valid sudoku puzzle.\nPlease make sure you haven't placed any duplicate numbers in the same region.\n");
						haveValidInput = false;
						break validate;
					} else presInRow[puzzle[i][j]] = presInCol[puzzle[j][i]] = presInBox[puzzle[boxRow][boxCol]] = true;
				}
			}
		}

		// Solve the sudoku puzzle
		SudokuSolver solver = new SudokuSolver(puzzle);

		// Print the results
		int validity = solver.checkValidity();
		System.out.println();
		if (validity == 0) {
			System.out.println("It doesn't appear that this puzzle has any valid solutions.");
		} else {
			if (validity == 1)
				System.out.println("This puzzle has exactly one solution:");
			else
				System.out.println("This puzzle has more than one solution, but here's one of them:");
			printPuzzle(solver.getSolvedPuzzle());
		}
	}

	/**
	 * Helper method for printing sudoku puzzles in a uniform grid
	 *
	 * @param puzzle The puzzle to print
	 */
	private static void printPuzzle(int[][] puzzle) {
		System.out.println();
		for (int i = 0; i < puzzle.length; i++) {
			for (int j = 0; j < puzzle[i].length; j++) {
				System.out.print((puzzle[i][j] == 0 ? "_" : puzzle[i][j]) + "    " + (j % 3 == 2 ? "  " : ""));
			}
			System.out.println("\n" + (i % 3 == 2 ? "\n" : ""));
		}
	}
}
/*
Sample output:


*/