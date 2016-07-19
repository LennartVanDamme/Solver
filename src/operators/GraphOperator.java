package operators;

import core.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lennart on 5/07/16.
 *
 * TODO: Flip mechanisme
 */
public class GraphOperator implements Operator {

    private final int D;
    private Solver solver;
    private Set<String> variablesFlipped;

    public GraphOperator(Solver solver, int graphDepth) {
        this.D = graphDepth;
        this.solver = solver;
    }

    @Override
    public void doMove(Solution solution) {

        /* Initialisatie van parameters */

        Set<String> variablesToKeep = new HashSet<>();
        Set<Integer> cliquesVisited = new HashSet<>();
        variablesFlipped = new HashSet<>();
        Set<String> elementsInCurrentDepth = new HashSet<>();
        Set<String> elementsVisitedInCurrentDepth = new HashSet<>();

        // Selecteren van een violated constraint, gebeurt met roulette
        // Indien er geen violated constraints zijn moet de move niet uitgevoerd worden.
        Constraint selectedConstriant = solver.getViolatedConstraint();
        if (selectedConstriant == null) return;

        // Beslissingsvariabele wordt gekozen met roulette uit constraint
        DecisionVariable temp = solver.getDecisionVaraible(selectedConstriant.chooseRandomVariableWithProb());
        // Breedte eerst itereren, met als root element temp.
        BreadthFirstIterator<DecisionVariable, DefaultEdge> breadthFirstIterator = new BreadthFirstIterator<>(solver.getConflictingGraph(), temp);

        int currentDepth = 0;
        int elementsToDepthIncrease = 1;
        int nextElementsToDepthIncrease = 0;
        elementsInCurrentDepth.add(temp.getName());

        while(breadthFirstIterator.hasNext()){
            temp = breadthFirstIterator.next();
            elementsInCurrentDepth.remove(temp.getName());
            elementsVisitedInCurrentDepth.add(temp.getName());

            /* Kijken welke variabelen geflipped moeten worden */

            Set<Integer> cliquesWithCurrentVariable = temp.getCliquesContainingVariable();
            for(Integer cliqueID : cliquesWithCurrentVariable){
                Clique currentClique = solver.getClique(cliqueID);
                if (currentClique.isViolated() && !cliquesVisited.contains(cliqueID)){
                    Set<String> variablesToFlip = currentClique.solve(variablesToKeep);
                    for(String variable : variablesToFlip){
                        solver.getSolution().flipVariable(variable);
                        variablesFlipped.add(variable);
                    }
                } else if (currentClique.getTotalActive() == 0 && !cliquesVisited.contains(cliqueID)){
                    String variableToFlip = currentClique.roulettePickVariable();
                    solver.getSolution().flipVariable(variableToFlip);
                    variablesFlipped.add(variableToFlip);
                }
                variablesToKeep.addAll(currentClique.getVariableNames());
                cliquesVisited.add(cliqueID);
            }

            nextElementsToDepthIncrease += checkUniqueNodes(elementsInCurrentDepth,elementsVisitedInCurrentDepth ,temp.getConflictingDecisionVariables());
            if(--elementsToDepthIncrease == 0){
                currentDepth++;
                if(currentDepth == D) { return;
                };
                elementsToDepthIncrease = nextElementsToDepthIncrease;
                nextElementsToDepthIncrease = 0;
            }
        }
    }

    @Override
    public void undoMove(Solution solution) {
        for(String variableName : variablesFlipped){
            solver.getSolution().flipVariable(variableName);
        }
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
