package operators;

import core.Constraint;
import core.DecisionVariable;
import core.Solution;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;
import core.Solver;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lennart on 5/07/16.
 *
 * TODO: Updaten van variablen op huidig niveau. Onderscheid tussen niveau moet gemaakt kunnen worden.
 * TODO: Flip mechanisme
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

        Constraint selectedConstriant = solver.getViolatedConstraint();
        if (selectedConstriant == null) solver.getRandomConstraint();
        DecisionVariable variable = solver.getDecisionVaraible(selectedConstriant.chooseRandomVariableWithProb());
        DecisionVariable temp;
        BreadthFirstIterator<DecisionVariable, DefaultEdge> breadthFirstIterator = new BreadthFirstIterator<>(solver.getConflictingGraph(), variable);

        int currentDepth = 0;
        int elementsToDepthIncrease = 1;
        int nextElementsToDepthIncrease = 0;
        Set<String> elementsInCurrentDepth = new HashSet<>();
        Set<String> elementsVisitedInCurrentDepth = new HashSet<>();
        elementsInCurrentDepth.add(variable.getName());

        while(breadthFirstIterator.hasNext()){
            temp = breadthFirstIterator.next();
            elementsInCurrentDepth.remove(temp.getName());
            elementsVisitedInCurrentDepth.add(temp.getName());
            System.out.println(temp);
            nextElementsToDepthIncrease += checkUniqueNodes(elementsInCurrentDepth,elementsVisitedInCurrentDepth ,temp.getConflictingDecisionVariables());
            if(--elementsToDepthIncrease == 0){
                currentDepth++;
                if(currentDepth == D) return;
                else{

                }
                elementsToDepthIncrease = nextElementsToDepthIncrease;
                nextElementsToDepthIncrease = 0;
            }
        }
    }

    @Override
    public void undoMove(Solution solution) {

    }

    private int checkUniqueNodes(Set<String> elementsInCurrentDepth,Set<String> elementsVisitedInCurrentDepth ,Set<String> conflictingVariablesOfCurrentNode){
        int uniqueNodes = 0;
        for(String conflictingVariable : conflictingVariablesOfCurrentNode){
            if(!elementsInCurrentDepth.contains(conflictingVariable) && !elementsVisitedInCurrentDepth.contains(conflictingVariable)){
                elementsInCurrentDepth.add(conflictingVariable);
                uniqueNodes++;
            }
        }
        return uniqueNodes;
    }
}
