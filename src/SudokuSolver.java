/**
 * Implementation of SudokuManipulator used for solving puzzles
 */
public class SudokuSolver extends SudokuManipulator {
	/**
	 * Primary (and only) constructor
	 *
	 * @param initialInfo 9x9 2D int array with all known puzzle cells filled in
	 */
	public SudokuSolver(int[][] initialInfo) {
		super(initialInfo);
	}

	/**
	 * Takes the provided condition and converts its list of Nodes into
	 * an array of corresponding PossibilityHeaders that retain the original order
	 *
	 * @param condition The condition being checked
	 * @return An ordered array of PossibilityHeaders to use
	 */
	@Override
	protected PossibilityHeader[] orderPossibilities(ConditionHeader condition) {
		return condition.getNodes().stream().map(Node::getPossibility).toArray(PossibilityHeader[]::new);
	}

	/**
	 * Checks whether the puzzle had a valid number of solutions
	 *
	 * @return 0 if the puzzle had zero solutions, 1 if the puzzle had exactly one solution, 2 if the puzzle had more than one solution
	 */
	public int checkValidity() {
		return solutionValidity;
	}
}

/*		for (int i = 0; i < possibilities.length; i++)
			possibilities[i] = condition.nodes.get(i).possibility;
		for (int i = 0; i < possibilities.length - 1; i++) {

		}*/