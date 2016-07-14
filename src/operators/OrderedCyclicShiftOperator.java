package operators;

import core.Solution;
import solver.Solver;

/**
 * Created by Lennart on 5/07/16.
 */
public class OrderedCyclicShiftOperator implements Operator {

    private Solver solver;

    public OrderedCyclicShiftOperator(Solver solver) {
        this.solver = solver;
    }

    @Override
    public void doMove(Solution solution) {

    }

    @Override
    public void undoMove(Solution solution) {

    }
}
