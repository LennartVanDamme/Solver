package core;

import java.util.*;

/**
 * This class represents a decision variable of an Binary Programming problem
 *
 * @author Lennart Van Damme
 */
public class DecisionVariable extends Observable implements Comparable<DecisionVariable> {

    private String name;
    private String boundType;
    private boolean inObjective;
    private Set<String> constraints;
    private Set<String> conflictingDecisionVariables;
    private Set<Long> conflictingGroups;
    private List<Integer> cliquesContainingVariable;
    private int value;
    private int bound;

    /**
     * Construncts a DecisionVariable object.
     */
    public DecisionVariable() {
        name = "Dummy";
        boundType = "N";
        inObjective = false;
        constraints = new HashSet<>();
        cliquesContainingVariable = new LinkedList<>();
        conflictingDecisionVariables = new HashSet<>();
        conflictingGroups = new HashSet<>();
        value = 0;
    }

    /**
     * Constructs a DecisionVariable object with a specified name.
     *
     * @param name Name of the decisision variable.
     */
    public DecisionVariable(String name) {
        this.name = name;
        inObjective = false;
        constraints = new HashSet<>();
        conflictingDecisionVariables = new HashSet<>();
        conflictingGroups = new HashSet<>();
        cliquesContainingVariable = new LinkedList<>();
        value = 0;
    }

    /**
     * Makes a deep copy of a DecisionVariable
     *
     * @param decisionVariable DecisionVariable object of which a deep copy is to be made.
     */
    public DecisionVariable(DecisionVariable decisionVariable) {
        name = decisionVariable.getName();
        boundType = decisionVariable.getBoundType();
        inObjective = decisionVariable.isInObjective();
        constraints = new HashSet<>(decisionVariable.getConstraints());
        conflictingDecisionVariables = new HashSet<>(decisionVariable.getConflictingDecisionVariables());
        conflictingGroups = new HashSet<>(decisionVariable.getConflictingGroups());
        cliquesContainingVariable = new LinkedList<>(decisionVariable.getCliquesContainingVariable());
        value = decisionVariable.getValue();
        bound = decisionVariable.getBound();
    }

    /**
     * @return Returns name of the decision variable.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Name for the decision variable
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getBoundType() {
        return boundType;
    }

    public void setBoundType(String boundType) {
        this.boundType = boundType;
    }

    public boolean isInObjective() {
        return inObjective;
    }

    public void setInObjective(boolean inObjective) {
        this.inObjective = inObjective;
    }

    public Set<String> getConstraints() {
        return constraints;
    }

    public Set<String> getConflictingDecisionVariables() {
        return conflictingDecisionVariables;
    }

    public void setConflictingDecisionVariables(Set<String> conflictingDecisionVariables) {
        this.conflictingDecisionVariables = conflictingDecisionVariables;
    }

    public Set<Long> getConflictingGroups() {
        return conflictingGroups;
    }


    public void setConflictingGroup(Set<Long> conflictingGroupDecisionVariables) {
        this.conflictingGroups = conflictingGroupDecisionVariables;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        int prevValue = this.value;
        this.value = value;
        setChanged();
        notifyObservers("" + prevValue);
    }

    public int getBound() {
        return bound;
    }

    public void setBound(int bound) {
        this.bound = bound;
    }

    public List<Integer> getCliquesContainingVariable() {
        return cliquesContainingVariable;
    }

    public void setCliquesContainingVariable(List<Integer> cliquesContainingVariable) {
        this.cliquesContainingVariable = cliquesContainingVariable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return name + ", value=" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DecisionVariable other = (DecisionVariable) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int compareTo(DecisionVariable o) {
        // TODO Auto-generated method stub
        return name.compareTo(o.getName());
    }

    public void addConflictingGroup(long hash) {
        conflictingGroups.add(hash);
    }

    public boolean decisionVariableIsConflicting(String decisionVariable) {
        return conflictingDecisionVariables.contains(decisionVariable);
    }

    public boolean checkBinary() {
        if (boundType.equals("BV")) {
            return true;
        } else return boundType.equals("UP") && bound == 1;
    }

    public void addConflictingVariable(String key) {
        conflictingDecisionVariables.add(key);
    }

    public void printOutConflictingVariables() {
        System.out.print("Variabele " + name + " is in conflict met: \n");
        for (String str : conflictingDecisionVariables) {
            System.out.println("\t" + str);
        }
    }

    public boolean possibleToSet(Solution solution) {
        return false;
    }

    public void addClique(int iD) {
        cliquesContainingVariable.add(iD);
    }

    public int flipValue() {
        value = 1 - value;
        setChanged();
        notifyObservers();
        return value;
    }

    public boolean checkGroupsIsOverlapping(Long hash) {
        long hashOfName = name.hashCode();
        for (Long hashOfConflictingGroups : conflictingGroups) {
            if (hashOfConflictingGroups + hashOfName == hash) return true;
        }
        return false;
    }


}
