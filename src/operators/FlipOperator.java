package operators;

import core.Constraint;
import core.Solution;
import core.Solver;

public class FlipOperator implements Operator {

    private String chosenDecisionVariableName;

    private Solver solver;

    public FlipOperator(Solver solver) {
        this.solver = solver;
    }


    @Override
    public void doMove(Solution solution) {
        Constraint infeasibleConstraint = solver.getViolatedConstraint();
        if (infeasibleConstraint != null) {
            chosenDecisionVariableName = infeasibleConstraint.chooseRandomVariableWithProb();

        } else {
            chosenDecisionVariableName = solution.giveRandomVariable();
        }
        solution.flipVariable(chosenDecisionVariableName);
    }

    @Override
    public void undoMove(Solution solution) {
        solution.flipVariable(chosenDecisionVariableName);
    }

    public String toString() {
        return "Flip operator";
    }
}
