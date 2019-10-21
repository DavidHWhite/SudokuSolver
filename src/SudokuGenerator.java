/**
 * Implementation of SudokuManipulator used for generating new sudoku puzzles
 */
public class SudokuGenerator extends SudokuManipulator {
	/**
	 * Primary (and only) constructor
	 */
	public SudokuGenerator() {
		// Solve an empty board
		super();

		// Remove numbers from the solved board until there's more than one valid solution, then add back one number to get the unsolved board
		unsolved = copyArray2D(solution);
		int row, col, val;
		SudokuSolver tester;
		do {
			row = (int) (Math.random() * 9);
			col = (int) (Math.random() * 9);
			val = unsolved[row][col];
			unsolved[row][col] = 0;
			tester = new SudokuSolver(unsolved);
		} while (tester.checkValidity() < 2);
		unsolved[row][col] = val;
	}

	/**
	 * Takes the provided condition and converts its list of Nodes into
	 * an array of corresponding PossibilityHeaders in a random order
	 *
	 * @param condition The condition being checked
	 * @return An ordered array of PossibilityHeaders to use
	 */
	@Override
	protected PossibilityHeader[] orderPossibilities(ConditionHeader condition) {
		PossibilityHeader[] possibilities = condition.getNodes().stream().map(Node::getPossibility).toArray(PossibilityHeader[]::new);
		for (int i = 0; i < possibilities.length - 1; i++) {
			int rand = (int) (Math.random() * (possibilities.length - i));
			PossibilityHeader temp = possibilities[i];
			possibilities[i] = possibilities[rand];
			possibilities[rand] = temp;
		}
		return possibilities;
	}
}

