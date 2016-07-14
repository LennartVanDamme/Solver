package core;

import solver.Heuristic;

import java.util.*;

public class Constraint extends Observable implements Observer, Comparable<Constraint> {

    public static final int NON_RESTRICTIVE = 0;
    public static final int SET_COVERING_CONSTRAINT = 1;
    public static final int SET_PACKING_CONSTRAINT = 2;
    public static final int EQUALITY = 3;
    public static final int OBJECTIVE = 4;

    static final int BECAME_FEASIBLE = 0;
    static final int BECAME_INFEASIBLE = 1;
    static final int SAME_STATE = 2;

    private String name;
    private int type;
    private Map<String, Double> coefficients;
    private Map<String, Integer> variableValues;
    private double constraintValue;
    private boolean feasible;
    private double totalOfCoefficients;
    private double constraintDelta;

    private String[] weightedVariableNames;
    private Double[] weightsInConstraint;

    private double rightHandSide;
    private double h;
    private double u;

    private boolean ranged;

    public Constraint(String name, int type) {
        this.name = name;
        this.type = type;
        ranged = false;
        coefficients = new HashMap<>();
        variableValues = new HashMap<>();
        constraintValue = 0;
        totalOfCoefficients = 0;
        constraintDelta = 0;
    }


    public double getRightHandSide() {
        return rightHandSide;
    }

    public void setRightHandSide(double rightHandSide) {
        this.rightHandSide = rightHandSide;
    }

    public Map<String, Integer> getVariableValues() {
        return variableValues;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public String getName() {
        return name;
    }

    Map<String, Double> getCoefficients() {
        return coefficients;
    }

    public int getType() {
        return type;
    }

    public double getConstraintDelta() {
        return constraintDelta;
    }

    private void initiateWeigthArrays() {

        weightedVariableNames = new String[coefficients.size()];
        weightedVariableNames = coefficients.keySet().toArray(weightedVariableNames);

        weightsInConstraint = new Double[coefficients.size()];
        int i = 0;
        if (type == Constraint.SET_COVERING_CONSTRAINT) {
            for (Double coefficient : coefficients.values()) {
                weightsInConstraint[i] = 1.0 - (coefficient / totalOfCoefficients);
                i++;
            }
        } else {
            for (Double coefficient : coefficients.values()) {
                weightsInConstraint[i] = (coefficient / totalOfCoefficients);
                i++;
            }
        }

        weightsInConstraint = coefficients.values().toArray(weightsInConstraint);
    }


    public void addCoefficient(String decisionVarName, double coefficient) {
        coefficients.put(decisionVarName, coefficient);
        variableValues.put(decisionVarName, 0);
        totalOfCoefficients += coefficient;
    }

    /**
     * This method is used to determine the degree in which a constraint is violated
     *
     */
    public void updateConstraintDelta() {
        if (checkIfFeasible(constraintValue)) {
            constraintDelta = 0;
        } else constraintDelta = Math.abs(constraintValue - rightHandSide);
    }

    // Constraint types: N = 0, G = 1, L = 2, E = 3
    public void calculateRange(double r) {
        ranged = true;
        if (type == SET_COVERING_CONSTRAINT) {
            h = rightHandSide;
            u = rightHandSide + Math.abs(r);
        } else if (type == SET_PACKING_CONSTRAINT) {
            h = rightHandSide - Math.abs(r);
            u = rightHandSide;
        } else if (type == EQUALITY) {
            if (r >= 0) {
                h = rightHandSide;
                u = rightHandSide + r;
            } else {
                h = rightHandSide + r;
                u = rightHandSide;
            }
        }
    }


    /**
     * This method checks if the constraint is feasible for a given value.
     * @param value value of the constraint.
     * @return Returns true when the constraint is feasible for the given value, and false if not.
     */
    // Constraint types: N = 0, G = 1, L = 2, E = 3
    boolean checkIfFeasible(double value) {
        if (!ranged) {
            if (type == SET_COVERING_CONSTRAINT) {
                return value >= rightHandSide;
            } else if (type == SET_PACKING_CONSTRAINT) {
                return value <= rightHandSide;
            } else if (type == EQUALITY) {
                return rightHandSide == value;
            } else {
                return true;
            }
        } else {
            return (h <= value && value <= u);
        }
    }

    /**
     * This methods checks if the constraint is feasbible using its current constraint value.
     *
     * @return Returns true when the constraint is feasible for the given value, and false if not.
     */
    public boolean checkIfFeasible() {
        if (!ranged) {
            if (type == SET_COVERING_CONSTRAINT) {
                feasible = (constraintValue >= rightHandSide);
            } else if (type == SET_PACKING_CONSTRAINT) {
                feasible = (constraintValue <= rightHandSide);
            } else if (type == EQUALITY) {
                feasible = (rightHandSide == constraintValue);
            } else {
                feasible = true;
            }
        } else {
            feasible = (h <= constraintValue && constraintValue <= u);
        }
        return feasible;
    }

    @Override
    public String toString() {
        String constraint = new String();
        if (ranged) {
            constraint += h + " <= ";
        }
        for (Map.Entry<String, Double> entry : coefficients.entrySet()) {
            if (constraint.length() > 4) {
                constraint += " + " + entry.getValue() + entry.getKey();
            } else {
                constraint += entry.getValue() + entry.getKey();
            }
        }

        if (ranged) {
            constraint += " <= " + u;
        } else if (type == EQUALITY) {
            constraint += " = " + rightHandSide;
        } else if (type == SET_PACKING_CONSTRAINT) {
            constraint += " <= " + rightHandSide;
        } else if (type == SET_COVERING_CONSTRAINT) {
            constraint += " >= " + rightHandSide;
        }
        return constraint;
    }


    @Override
    public void update(Observable o, Object arg) {

        /**
         * Checkt aan de hand van de huidige waarde van de beslissingsvariabele en de vorige (die bijgehouden wordt in de map variablesValues)
         * of de coeffiecient van die variabele moet opgeteld of afgetrokken worden bij de totale waarde van de constraint.
         * Nadien wordt kekenen of de constraint feasible is of niet.
         * Het resultaat wordt gemarkeerd als changed en het model zal zijn lijst van infeasible constraints kunnen aanpassen.
         * */

        DecisionVariable variable = (DecisionVariable) o;

        if (variable.getValue() == 1) {
            constraintValue += coefficients.get(variable.getName());
        } else {
            constraintValue -= coefficients.get(variable.getName());
        }
        boolean wasFeasible = feasible;
        feasible = checkIfFeasible(constraintValue);
        variableValues.put(variable.getName(), variable.getValue());
        updateConstraintDelta();
        setChanged();
        if (!wasFeasible && feasible) {
            notifyObservers(BECAME_FEASIBLE);
        } else if (wasFeasible && !feasible) {
            notifyObservers(BECAME_INFEASIBLE);
        } else {
            notifyObservers(SAME_STATE);
        }

    }

    public void calculateValue() {
        for (Map.Entry<String, Integer> entry : variableValues.entrySet()) {
            constraintValue += entry.getValue() * coefficients.get(entry.getKey());
        }
    }

    /**
     * Chooses a variable name using a roulette search based on the coefficient of the {@link DecisionVariable} in the {@link Constraint}.
     * For packing constraints the {@link DecisionVariable} with the largest coefficient in the {@link Constraint} has the highest probability of beeing picked.
     * For covering constraint the {@link DecisionVariable} with the lowest coefficient int the {@link Constraint} has the highest probability of beeing picked.
     *
     * @return The name of the selected {@link DecisionVariable} object.
     */
    public String chooseRandomVariableWithProb() {
        if (weightsInConstraint == null) {
            initiateWeigthArrays();
        }
        double value = Heuristic.RANDOM.nextDouble() * totalOfCoefficients;
        // locate the random value based on the weights
        for (int i = 0; i < weightsInConstraint.length; i++) {
            value -= (weightsInConstraint[i] - ((weightsInConstraint[i] / 2) * variableValues.get(weightedVariableNames[i])));
            if (value <= 0) return weightedVariableNames[i];
        }
        // only when rounding errors occur
        return weightedVariableNames[weightsInConstraint.length - 1];
    }

    public Set<String> chooseVariablesWithProb() {
        int nVariables = Heuristic.RANDOM.nextInt(coefficients.size());
        if (weightsInConstraint == null) {
            initiateWeigthArrays();
        }
        Set<String> choosenVariables = new HashSet<>();
        double value;
        for (int i = 0; i < nVariables; i++) {
            value = Heuristic.RANDOM.nextDouble() * totalOfCoefficients;
            for (int j = 0; j < weightsInConstraint.length; j++) {
                value -= (weightsInConstraint[j] - ((weightsInConstraint[j] / 2) * variableValues.get(weightedVariableNames[j])));
                if (value <= 0) choosenVariables.add(weightedVariableNames[j]);
            }
            if (value > 0) {
                for (String variableName : getCoefficients().keySet()) {
                    if (!choosenVariables.contains(variableName)) {
                        choosenVariables.add(variableName);
                        break;
                    }
                }
            }
        }
        return choosenVariables;
    }

    /**
     * Chooses a random name of a {@link DecisionVariable} object in the {@link Constraint}. This is done by using {@link java.util.Random}.
     *
     * @return The name of the selected {@link DecisionVariable} object.
     */
    public String chooseRandaomVariable() {
        return weightedVariableNames[Heuristic.RANDOM.nextInt(weightedVariableNames.length)];
    }

    public int size() {
        return coefficients.size();
    }

    @Override
    public int compareTo(Constraint constraint) {
        if (constraint.getConstraintDelta() == constraintDelta) return 0;
        else return constraint.getConstraintDelta() > constraintDelta ? 1 : -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Constraint that = (Constraint) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String getUniqueVariable(Set<String> selectedDecisionVariable) {
        for (String variableName : coefficients.keySet()) {
            if (selectedDecisionVariable.contains(variableName)) return variableName;
        }
        return null;
    }
}
