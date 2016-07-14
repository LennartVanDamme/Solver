package core;

import java.util.*;

/**
 * This class describes groups of decision variables that create a conflict when
 * all of them are active
 *
 * @author Lennart Van Damme
 */
public class ConflictingGroup extends DecisionVariable implements Observer {

    private int nActiveVariables;
    private List<String> toObserveDecisionVariables;
    private Map<String, DecisionVariable> decisionVariablesInGroup;
    private long hash;
    private Map<String, Double> combinedConstraintValues;

    public ConflictingGroup(long hash, List<String> toObserve) {
        super();
        this.hash = hash;
        toObserveDecisionVariables = new ArrayList<>(toObserve);
        decisionVariablesInGroup = new HashMap<>();
        super.setName(createName());
        nActiveVariables = 0;
        combinedConstraintValues = new HashMap<>();
    }

    /**
     * @return Returns the number of active variables
     */
    public int getnActiveVariables() {
        return nActiveVariables;
    }

    public long getHash() {
        return hash;
    }

    public List<String> getToObserveDecisionVariables() {
        return toObserveDecisionVariables;
    }

    @Override
    public String toString() {
        return "ConflictingGroup [name=" + getName() + ", value()=" + getValue() + "]";
    }

    private String createName() {
        String name = new String();
        for (int i = 0; i < getToObserveDecisionVariables().size(); i++) {
            name += getToObserveDecisionVariables().get(i);
            if (i < getToObserveDecisionVariables().size() - 1) {
                name += "&&";
            }
        }
        return name;
    }

    @Override
    public void update(Observable o, Object arg) {
        int nActive = 0;
        for (DecisionVariable variable : decisionVariablesInGroup.values()) {
            nActive += variable.getValue();
        }
        if (nActive == toObserveDecisionVariables.size()) {
            setValue(1);
        } else {
            setValue(0);
        }
    }


    /**
     * Adds a DecisionVariable object to the map of decision variables in the conflicting group.
     *
     * @param variable DecisionVariable object to be added.
     */
    public void addVariableToGroup(DecisionVariable variable) {
        decisionVariablesInGroup.put(variable.getName(), variable);
    }

    public Set<DecisionVariable> getSubSetConflictingVariables() {
        Set<DecisionVariable> subSet = new HashSet<>();
        List<String> variableNames = new LinkedList<String>(decisionVariablesInGroup.keySet());
        int i = 0;
        Random random = new Random();
        String variableName;
        while (i < decisionVariablesInGroup.size() - 1) {
            variableName = variableNames.get(random.nextInt(decisionVariablesInGroup.size()));
            if (!subSet.contains(decisionVariablesInGroup.get(variableName))) {
                subSet.add(decisionVariablesInGroup.get(variableName));
                i++;
            }
        }
        return subSet;
    }

    public void addConstraintValue(String constraintName, double value) {
        combinedConstraintValues.put(constraintName, value);
    }

    public double getCombinedvalueOfConstraint(String constraintName) {
        return combinedConstraintValues.get(constraintName);
    }

    public boolean variableInGroup(String variableName) {
        return toObserveDecisionVariables.contains(variableName);
    }

    public boolean checkGroupsIsOverlapping(Long hashOfGroupToCheck) {
        for (Long conflictingHash : getConflictingGroups()) {
            if (conflictingHash + hash == hashOfGroupToCheck) return true;
        }
        return false;
    }

}
