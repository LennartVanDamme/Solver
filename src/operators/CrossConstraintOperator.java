package operators;

import core.Constraint;
import core.DecisionVariable;
import core.Solution;
import core.Heuristic;
import core.Solver;

import java.util.HashSet;
import java.util.Set;

/**
 * This class will select a random decision variable and then check all the constraints its in.
 * It will then evaluate the effect of flipping the variable and if the flip is its is beneficial the new state will be saved.
 * <p>
 * Created by Lennart on 5/07/16.
 */
public class CrossConstraintOperator implements Operator {

    private final int K;
    private Set<String> selectedDecisionVariable;
    private Solver solver;

    public CrossConstraintOperator(Solver solver, int crossConstraintParameter) {
        this.solver = solver;
        this.K = crossConstraintParameter;
        selectedDecisionVariable = new HashSet<>();
    }


    @Override
    public void doMove(Solution solution) {
        int nConstraintsSelected = 0;
        Constraint constraint, selectedConstraint;

        selectedConstraint = solver.getViolatedConstraint();
        if (selectedConstraint == null) selectedConstraint = solver.getRandomConstraint();

        double prevConstaintDelta = selectedConstraint.getConstraintDelta();
        for (String variableName : selectedConstraint.chooseVariablesWithProb()) {
            solver.getSolution().flipVariable(variableName);
            if (selectedConstraint.getConstraintDelta() < prevConstaintDelta) {
                selectedDecisionVariable.add(variableName);
            } else {
                solver.getSolution().flipVariable(variableName);
            }
        }
        nConstraintsSelected++;

        String[] selectedVariables = selectedDecisionVariable.stream().toArray(String[]::new);

        while (nConstraintsSelected < K) {
            if (selectedVariables.length > 0) {
                constraint = solver.getViolatedConstraint(selectedVariables[Heuristic.RANDOM.nextInt(selectedVariables.length)]);
            } else {
                constraint = solver.getRandomConstraint();
            }
            if (constraint == null) constraint = solver.getRandomConstraint();
            prevConstaintDelta = selectedConstraint.getConstraintDelta();
            DecisionVariable variable = solver.getDecisionVaraible(constraint.getUniqueVariable(selectedDecisionVariable));
            if (variable != null) {
                variable.flipValue();
                if (selectedConstraint.getConstraintDelta() >= prevConstaintDelta) {
                    variable.flipValue();
                } else {
                    selectedDecisionVariable.add(variable.getName());
                }
            }
            nConstraintsSelected++;
        }

    }

    @Override
    public void undoMove(Solution solution) {
        for (String variableName : selectedDecisionVariable) {
            solver.getSolution().flipVariable(variableName);
        }
    }

    public String toString() {
        return "Cross constraint operator";
    }

}
