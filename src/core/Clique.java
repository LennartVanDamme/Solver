package core;

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
    private boolean violated;
    private Solver solver;
    private int totalActive;

    public Clique(int id, Solver solver) {
        ID = id;
        violated = false;
        this.solver = solver;
        totalActive = 0;
        this.decisionVariables = new LinkedList<>();
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
    void connectDecisionVariables() {
        for (DecisionVariable variable : decisionVariables) {
            variable.addClique(ID);
        }
    }

    void addVariables(Set<DecisionVariable> variableSet){
        decisionVariables.addAll(variableSet);
        Collections.sort(decisionVariables, new Comparator<DecisionVariable>() {
            @Override
            public int compare(DecisionVariable variable1, DecisionVariable variable2) {

                double coeffVariable1 = solver.getObjectiveFunction().getCoefficientForVariable(variable1.getName());
                double coeffVariable2 = solver.getObjectiveFunction().getCoefficientForVariable(variable2.getName());

                if(Heuristic.MINIMIZE){
                    if(coeffVariable1 > coeffVariable2) return 1;
                    if(coeffVariable1 < coeffVariable2) return -1;
                } else {
                    if(coeffVariable1 > coeffVariable2) return -1;
                    if(coeffVariable1 < coeffVariable2) return 1;
                }

                return 0;
            }
        });
    }


    @Override
    public String toString() {
        return "Clique [ID=" + ID + ", decisionVariables=" + decisionVariables + "]";
    }


    @Override
    public void update(Observable arg0, Object arg1) {
        DecisionVariable var = (DecisionVariable) arg0;
        if(var.getValue() == 1) totalActive++;
        else totalActive--;
        violated = totalActive > 1;


    }

}
