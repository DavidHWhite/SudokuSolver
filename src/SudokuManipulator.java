import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class containing code for solving sudoku puzzles
 */
public abstract class SudokuManipulator {

	//******************//
	//***** Fields *****//
	//******************//

	int[][] solution;
	int[][] unsolved;
	protected int solutionValidity;

	//**************************//
	//***** Public Methods *****//
	//**************************//

	/**
	 * Default constructor (solves an empty board)
	 */
	public SudokuManipulator() {
		this(new int[][] {{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}});
	}

	/**
	 * Primary constructor
	 *
	 * @param initialInfo 9x9 2D int array with all known puzzle cells filled in
	 */
	public SudokuManipulator(int[][] initialInfo) {
		unsolved = copyArray2D(initialInfo);
		solution = null;
		solutionValidity = 0;

		// Create the conditions
		ConditionHeader[] conditions = new ConditionHeader[324];
		List<ConditionHeader> conditionsRemaining = new ArrayList<>();
		for (int i = 0; i < 324; i++) {
			conditions[i] = new ConditionHeader();
			conditionsRemaining.add(conditions[i]);
		}
		for (int i = 1; i < 324; i++) {
			ConditionHeader previousC = conditions[i - 1], currentC = conditions[i];
			previousC.setNextH(currentC);
			currentC.setPreviousH(previousC);
		}
		conditions[0].setPreviousH(conditions[323]);
		conditions[323].setNextH(conditions[0]);

		// Create the possibilities and flesh out the matrix
		PossibilityHeader[] possibilities = new PossibilityHeader[729];
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
				for (int val = 1; val <= 9; val++) {
					// Create possibility header
					PossibilityHeader possibility = new PossibilityHeader(row, col, val);
					possibilities[getPossibilityIndex(row, col, val)] = possibility;

					// List the conditions fulfilled by placing a particular val in the cell at (row, col)
					int[] conditionsFulfilled = new int[] {
						/* Cell */   col + (row * 9),
						/* Row */    80 + val + (row * 9),
						/* Column */ 161 + val + (col * 9),
						/* Box */    242 + val + ((col / 3) + 3 * (row / 3)) * 9
						/* "Box" refers to the 3x3 regions of the game board */
					};

					// Create a new Node for each fulfilled condition and add it to the matrix
					for (int cIndex : conditionsFulfilled) {
						ConditionHeader condition = conditions[cIndex];
						Node node = new Node(condition, possibility);
						possibility.addNode(node);
						condition.addNode(node);
					}
				}

		// Fill in the known information
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				int val = initialInfo[row][col];

				// Check that a value was selected for the cell
				if (val != 0) {
					// Get the correct possibility header
					PossibilityHeader poss = possibilities[getPossibilityIndex(row, col, val)];

					// Update the possibility matrix
					Node currPrimaryN = poss.getFirstNode();
					do {
						ConditionHeader conditionToRemove = currPrimaryN.getCondition();
						conditionsRemaining.remove(conditionToRemove);
						conditionToRemove.getPreviousH().setNextH(conditionToRemove.getNextH());
						conditionToRemove.getNextH().setPreviousH(conditionToRemove.getPreviousH());
						for (Node currSecondaryN = currPrimaryN.getDown(); currSecondaryN != currPrimaryN; currSecondaryN = currSecondaryN.getDown()) {
							for (Node currTertiaryN = currSecondaryN.getRight(); currTertiaryN != currSecondaryN; currTertiaryN = currTertiaryN.getRight()) {
								currTertiaryN.getCondition().getNodes().remove(currTertiaryN);
								currTertiaryN.getUp().setDown(currTertiaryN.getDown());
								currTertiaryN.getDown().setUp(currTertiaryN.getUp());
							}
						}
						currPrimaryN = currPrimaryN.getRight();
					} while (currPrimaryN != poss.getFirstNode());
				}
			}
		}

		// Find the solutions, check how many there are, and store one of them
		solve(copyArray2D(unsolved), conditionsRemaining);
	}

	/**
	 * Gets the unsolved version of a puzzle.
	 *
	 * @return 9x9 int array containing the unsolved version of this sudoku puzzle with 0s used to represent empty cells
	 */
	public int[][] getUnsolvedPuzzle() {
		return unsolved;
	}

	/**
	 * Returns the fully solved puzzle
	 *
	 * @return The fully solved puzzle in the form of an int array or null if no valid solution was found
	 */
	public int[][] getSolvedPuzzle() {
		return solution;
	}

	//*************************************************************************************//
	//***** Abstract methods which change behavior when generating vs solving puzzles *****//
	//*************************************************************************************//

	protected abstract PossibilityHeader[] orderPossibilities(ConditionHeader condition);

	//***************************//
	//***** Private Methods *****//
	//***************************//

	private void solve(int[][] currentData, List<ConditionHeader> conditionsRemaining) {
		// Check if the current state is a solution or if too many solutions have already been found
		if (conditionsRemaining.isEmpty()) {
			solutionValidity++;
			if (solutionValidity < 2)
				solution = copyArray2D(currentData);
			return;
		}
		if (solutionValidity > 1)
			return;

		// Get the next ConditionHeader
		ConditionHeader initial = conditionsRemaining.get(0), current = initial.getNextH(), lowest = initial;
		while (current != initial) {
			if (current.getNodes().size() < lowest.getNodes().size())
				lowest = current;
			current = current.getNextH();
		}
		// Get the ordered list of PossibilityHeaders and iterate through it
		PossibilityHeader[] possibilitiesToTest = orderPossibilities(lowest);
		for (PossibilityHeader poss : possibilitiesToTest) {
			// Update the puzzle
			currentData[poss.getRow()][poss.getColumn()] = poss.getValue();

			// Update the possibility matrix
			Node currPrimaryN = poss.getFirstNode();
			do {
				// Update the matrix
				ConditionHeader conditionToRemove = currPrimaryN.getCondition();
				conditionsRemaining.remove(conditionToRemove);
				conditionToRemove.getPreviousH().setNextH(conditionToRemove.getNextH());
				conditionToRemove.getNextH().setPreviousH(conditionToRemove.getPreviousH());
				for (Node currSecondaryN = currPrimaryN.getDown(); currSecondaryN != currPrimaryN; currSecondaryN = currSecondaryN.getDown()) {
					for (Node currTertiaryN = currSecondaryN.getRight(); currTertiaryN != currSecondaryN; currTertiaryN = currTertiaryN.getRight()) {
						currTertiaryN.getCondition().getNodes().remove(currTertiaryN);
						currTertiaryN.getUp().setDown(currTertiaryN.getDown());
						currTertiaryN.getDown().setUp(currTertiaryN.getUp());
					}
				}
				currPrimaryN = currPrimaryN.getRight();
			} while (currPrimaryN != poss.getFirstNode());

			// Call this method recursively
			solve(currentData, conditionsRemaining);

			// Revert the matrix
			Node currPrimaryN2 = poss.getFirstNode().getLeft();
			do {
				ConditionHeader conditionToRemove = currPrimaryN2.getCondition();
				conditionsRemaining.add(conditionToRemove);
				conditionToRemove.getPreviousH().setNextH(conditionToRemove);
				conditionToRemove.getNextH().setPreviousH(conditionToRemove);
				for (Node currSecondaryN = currPrimaryN2.getUp(); currSecondaryN != currPrimaryN2; currSecondaryN = currSecondaryN.getUp()) {
					for (Node currTertiaryN = currSecondaryN.getLeft(); currTertiaryN != currSecondaryN; currTertiaryN = currTertiaryN.getLeft()) {
						currTertiaryN.getCondition().getNodes().add(currTertiaryN);
						currTertiaryN.getUp().setDown(currTertiaryN);
						currTertiaryN.getDown().setUp(currTertiaryN);
					}
				}
				currPrimaryN2 = currPrimaryN2.getLeft();
			} while (currPrimaryN2 != poss.getFirstNode().getLeft());
		}
	}

	/**
	 * Static helper method which returns the index of the possibility corresponding to a given row, column, and value
	 */
	private static int getPossibilityIndex(int row, int col, int val) {
		return (81 * row) + (9 * col) + (val - 1);
	}

	/**
	 * Static helper method which returns a deep copy of a 2D int array
	 */
	protected static int[][] copyArray2D(int[][] original) {
		int[][] duplicate = new int[original.length][original[0].length];
		for (int i = 0; i < original.length; i++)
			System.arraycopy(original[i], 0, duplicate[i], 0, original[0].length);
		return duplicate;
	}

	//*******************************************//
	//***** Inner Classes (matrix elements) *****//
	//*******************************************//

	class Node {
		private Node up, down, left, right;
		private ConditionHeader condition;
		private PossibilityHeader possibility;

		Node(ConditionHeader condition, PossibilityHeader possibility) {
			setUp(setDown(setLeft(setRight(this))));
			this.setCondition(condition);
			this.setPossibility(possibility);
		}

		//***** Accessors *****//

		Node getUp() {
			return up;
		}

		Node setUp(Node up) {
			this.up = up;
			return this;
		}

		Node getDown() {
			return down;
		}

		Node setDown(Node down) {
			this.down = down;
			return this;
		}

		Node getLeft() {
			return left;
		}

		Node setLeft(Node left) {
			this.left = left;
			return this;
		}

		Node getRight() {
			return right;
		}

		Node setRight(Node right) {
			this.right = right;
			return this;
		}

		ConditionHeader getCondition() {
			return condition;
		}

		Node setCondition(ConditionHeader condition) {
			this.condition = condition;
			return this;
		}

		PossibilityHeader getPossibility() {
			return possibility;
		}

		Node setPossibility(PossibilityHeader possibility) {
			this.possibility = possibility;
			return this;
		}
	}

	protected abstract class Header {
		private List<Node> nodes;

		Header() {
			setNodes(new ArrayList<>());
		}

		Node getFirstNode() {
			return getNodes().get(0);
		}

		public abstract void addNode(Node node);

		List<Node> getNodes() {
			return nodes;
		}

		Header setNodes(List<Node> nodes) {
			this.nodes = nodes;
			return this;
		}
	}

	protected class PossibilityHeader extends Header {
		private int row, column, value;

		PossibilityHeader(int row, int column, int value) {
			this.setRow(row);
			this.setColumn(column);
			this.setValue(value);
		}

		@Override
		public void addNode(Node node) {
			getNodes().add(node);
			Node firstNode = getNodes().get(0);
			firstNode.getLeft().setRight(node);
			node.setLeft(firstNode.getLeft());
			node.setRight(firstNode);
			firstNode.setLeft(node);
		}

		//***** Accessors *****//

		int getRow() {
			return row;
		}

		int getColumn() {
			return column;
		}

		int getValue() {
			return value;
		}

		PossibilityHeader setRow(int row) {
			this.row = row;
			return this;
		}

		PossibilityHeader setColumn(int column) {
			this.column = column;
			return this;
		}

		PossibilityHeader setValue(int value) {
			this.value = value;
			return this;
		}
	}

	protected class ConditionHeader extends Header {
		private ConditionHeader previousH, nextH;

		ConditionHeader() {
			setPreviousH(setNextH(null));
		}

		@Override
		public void addNode(Node node) {
			getNodes().add(node);
			Node firstNode = getNodes().get(0);
			firstNode.getUp().setDown(node);
			node.setUp(firstNode.getUp());
			node.setDown(firstNode);
			firstNode.setUp(node);
		}

		//***** Accessors *****//

		ConditionHeader getPreviousH() {
			return previousH;
		}

		ConditionHeader setPreviousH(ConditionHeader previousH) {
			this.previousH = previousH;
			return this;
		}

		ConditionHeader getNextH() {
			return nextH;
		}

		ConditionHeader setNextH(ConditionHeader nextH) {
			this.nextH = nextH;
			return this;
		}
	}
}

