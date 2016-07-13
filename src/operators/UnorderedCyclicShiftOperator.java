package operators;

import model.Solution;
import solver.Solver;

/**
 * Created by Lennart on 5/07/16.
 */
public class UnorderedCyclicShiftOperator implements Operator {

    private Solver solver;

    public UnorderedCyclicShiftOperator(Solver solver) {
        this.solver = solver;
    }

    @Override
    public void doMove(Solution solution) {

    }

    @Override
    public void undoMove(Solution solution) {

    }
}
