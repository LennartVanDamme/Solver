package operators;

import core.Constraint;
import core.Solution;
import solver.Solver;

import java.util.Map;

/**
 * Created by Lennart on 5/07/16.
 */
public class UnorderedCyclicShiftOperator implements Operator {

    private Solver solver;

    private String [] variableNames;
    private Integer [] originalValues;

    public UnorderedCyclicShiftOperator(Solver solver) {
        this.solver = solver;
    }

    @Override
    public void doMove(Solution solution) {

        Constraint constaint = solver.getViolatedConstraint();

        variableNames = constaint.getVariableValues().keySet().toArray(new String[]{});
        originalValues = constaint.getVariableValues().values().toArray(new Integer[]{});

        int temp;
        for (int i = 0; i < originalValues.length; i++){

        }
    }

    @Override
    public void undoMove(Solution solution) {

    }
}
