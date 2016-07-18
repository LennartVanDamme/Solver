package operators;

import core.Constraint;
import core.Solution;
import core.Heuristic;
import core.Solver;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lennart on 5/07/16.
 */
public class UnorderedCyclicShiftOperator implements Operator {

    private Solver solver;

    private Set<String> flippedVariables;

    public UnorderedCyclicShiftOperator(Solver solver) {
        this.solver = solver;
    }

    @Override
    public void doMove(Solution solution) {

        Constraint constaint = solver.getViolatedConstraint();
        flippedVariables = new HashSet<>();

        if(constaint.getVariableValues().size() == 1) return;
        String [] variableNames = constaint.getVariableValues().keySet().toArray(new String[]{});
        Integer [] originalValues = constaint.getVariableValues().values().toArray(new Integer[]{});

        int [] shiftedValues = new int[originalValues.length];
        int shiftKey = Heuristic.RANDOM.nextInt(originalValues.length-1);

        for (int i = 0; i < originalValues.length; i++){
            shiftedValues[(i+shiftKey)%originalValues.length] = originalValues[i];
        }

        for(int i = 0; i < originalValues.length; i++){
            if (originalValues[i] != shiftedValues[i]){
                solution.flipVariable(variableNames[i]);
                flippedVariables.add(variableNames[i]);
            }
        }
    }

    @Override
    public void undoMove(Solution solution) {
        for(String variableName : flippedVariables){
            solution.flipVariable(variableName);
        }
    }
}
