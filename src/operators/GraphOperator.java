package operators;

import model.Solution;
import solver.Solver;

/**
 * Created by Lennart on 5/07/16.
 */
public class GraphOperator implements Operator {

    private final int D;
    private Solver solver;

    public GraphOperator(Solver solver, int graphDepth) {
        this.D = graphDepth;
        this.solver = solver;
    }

    @Override
    public void doMove(Solution solution) {

    }

    @Override
    public void undoMove(Solution solution) {

    }
}
