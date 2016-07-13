package model;

import java.util.*;

/**
 * This class is used when set packing constraints are in the model.
 * Cliques are found by using the Bron-Kerbosch algorithm.
 *
 * @author Lennart Van Damme
 */
public class Clique implements Observer {

    private final int ID;
    private List<DecisionVariable> decisionVariables;
    private List<DecisionVariable> variablesOfSolution;
    private boolean violated;

    public Clique(int id, Set<DecisionVariable> decisionVariables) {
        ID = id;
        violated = false;
        this.decisionVariables = new ArrayList<>(decisionVariables);
        variablesOfSolution = new ArrayList<>();
    }

    /**
     * @return Returns a list of all decision variables in the clique.
     */
    public List<DecisionVariable> getDecisionVariables() {
        return decisionVariables;
    }

    /**
     * @return Returns the ID of the clique.
     */
    public int getID() {
        return ID;
    }

    public boolean isViolated() {
        return violated;
    }

    /**
     * Adds the ID of the clique to every decision variable in the clique.
     */
    public void connectDecisionVariables() {
        for (DecisionVariable variable : decisionVariables) {
            variable.addClique(ID);
        }
    }

    @Override
    public String toString() {
        return "Clique [ID=" + ID + ", decisionVariables=" + decisionVariables + "]";
    }

    public void addDecisionVariable(DecisionVariable var) {
        variablesOfSolution.add(var);
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        int totalActive = 0;
        for (DecisionVariable variable : variablesOfSolution) {
            totalActive += variable.getValue();
        }
        violated = totalActive > 1;


    }

}
