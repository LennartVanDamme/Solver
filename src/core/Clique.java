package core;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private double[] weightsOfVariables;
    private double totalWeight;

    public Clique(int id, Solver solver) {
        ID = id;
        violated = false;
        this.solver = solver;
        totalActive = 0;
        this.decisionVariables = new LinkedList<>();
        totalWeight = 0;
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

    public int getTotalActive() {
        return totalActive;
    }

    /**
     * Adds the ID of the clique to every decision variable in the clique.
     */
    void connectDecisionVariables() {
        for (DecisionVariable variable : decisionVariables) {
            variable.addClique(ID);
        }
    }

    void addVariables(Set<DecisionVariable> variableSet) {
        decisionVariables.addAll(variableSet);
        Collections.sort(decisionVariables, new Comparator<DecisionVariable>() {
            @Override
            public int compare(DecisionVariable variable1, DecisionVariable variable2) {

                double coeffVariable1 = solver.getObjectiveFunction().getCoefficientForVariable(variable1.getName());
                double coeffVariable2 = solver.getObjectiveFunction().getCoefficientForVariable(variable2.getName());
                if (Heuristic.MINIMIZE) {
                    if (coeffVariable1 == Double.MAX_VALUE && coeffVariable2 != Double.MAX_VALUE) return 1;
                    if (coeffVariable1 != Double.MAX_VALUE && coeffVariable2 == Double.MAX_VALUE) return -1;
                    if (coeffVariable1 > coeffVariable2) return 1;
                    if (coeffVariable1 < coeffVariable2) return -1;
                } else {
                    if (coeffVariable1 == Double.MAX_VALUE && coeffVariable2 != Double.MAX_VALUE) return -1;
                    if (coeffVariable1 != Double.MAX_VALUE && coeffVariable2 == Double.MAX_VALUE) return 1;
                    if (coeffVariable1 > coeffVariable2) return -1;
                    if (coeffVariable1 < coeffVariable2) return 1;
                }

                return 0;
            }
        });

    }

    void initiateWeightArrays() {
        weightsOfVariables = new double[decisionVariables.size()];
        int i = 0;
        double normalWeight = 1 / decisionVariables.size();
        double weightObjectiveVariable = normalWeight * 2;
        for (DecisionVariable variable : decisionVariables) {
            if (variable.isInObjective()) {
                weightsOfVariables[i] = weightObjectiveVariable;
                totalWeight += weightObjectiveVariable;
            } else {
                weightsOfVariables[i] = normalWeight;
                totalWeight += normalWeight;
            }
            i++;

        }
    }


    @Override
    public String toString() {
        return "Clique [ID=" + ID + ", decisionVariables=" + decisionVariables + "]";
    }


    @Override
    public void update(Observable arg0, Object arg1) {
        DecisionVariable var = (DecisionVariable) arg0;
        if (var.getValue() == 1) totalActive++;
        else totalActive--;
        violated = totalActive > 1;
    }

    /**
     * Returns variabelen die geflipped moeten worden met respect tot andere reeds bekenen cliques.
     * Als er geen variabelen geflipped moeten worden is de set leeg.
     */
    public Set<String> solve(Set<String> variablesToKeep) {
        Set<String> variablesTobeFlipped = new HashSet<>();
        DecisionVariable variable;
        int tempTotalActive = totalActive;
        int index;
        List<Integer> indices = new ArrayList<>();
        for(int i = 0; i < decisionVariables.size(); i++){
            indices.add(i);
        }

        while (tempTotalActive != 1 && indices.isEmpty()) {
            index = roulettePickVariable(indices);
            variable = decisionVariables.get(index);
            if (variable.getValue() == 1 && !variablesToKeep.contains(variable.getName())) {
                variablesTobeFlipped.add(variable.getName());
            }
            indices.remove((Integer) index);
        }

        return variablesTobeFlipped;
    }


    /**
     * Kiest een variabelen om te flippen in de clique op een roulette manier.
     *
     * @param indices Lijst met mogelijke indexes van beslissingsvariabelen waar nog uit gekozen kan worden.
     * @return Naam van de variabele om te flippen.
     */
    private int roulettePickVariable(List<Integer> indices) {
        double value = Heuristic.RANDOM.nextDouble() * totalWeight;
        int i = Heuristic.RANDOM.nextInt(indices.size());
        while (value > 0.0) {
            value -= weightsOfVariables[indices.get(i)];
            i = (i + 1) % indices.size();
        }
        return i;
    }

    public String roulettePickVariable() {
        double value = Heuristic.RANDOM.nextDouble() * totalWeight;
        for (int i = 0; i < weightsOfVariables.length; i++) {
            value -= weightsOfVariables[i];
            if (value <= 0.0) return decisionVariables.get(i).getName();
        }
        return decisionVariables.get(0).getName();
    }

    public Collection<String> getVariableNames() {
        return decisionVariables.stream().map(DecisionVariable::getName).collect(Collectors.toCollection(LinkedList::new));
    }
}
