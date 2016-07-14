package core;

import solver.Heuristic;

import java.util.Map;
import java.util.Observable;

public class ObjectiveFunction extends Constraint {

    private double penaltyCost;
    private double objectiveValue;
    private double feasibilityFactor;
    private double totalValue;

    public ObjectiveFunction(String name, int type) {
        super(name, type);
        penaltyCost = 0;
        objectiveValue = 0;
        totalValue = 0;
    }

    public double getFeasibilityFactor() {
        return feasibilityFactor;
    }

    public double getTotalValue() {
        return feasibilityFactor * penaltyCost + objectiveValue;
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public void calculateObjectivevalue() {
        totalValue += feasibilityFactor * penaltyCost;
        for (Map.Entry<String, Integer> entry : getVariableValues().entrySet()) {
            objectiveValue += entry.getValue() * getCoefficients().get(entry.getKey());
        }
        totalValue += objectiveValue;
    }

    public String randomElementFromObjective() {
        Object[] varNames = super.getCoefficients().keySet().toArray();
        return (String) varNames[Heuristic.RANDOM.nextInt(varNames.length)];
    }

    public void calculatePenaltyCost() {
        for (Double coefficient : super.getCoefficients().values()) {
            penaltyCost += coefficient;
        }
        if (!Heuristic.MINIMIZE) {
            penaltyCost *= -1;
        }
    }

    public double getPenaltyCost() {
        return penaltyCost;
    }

    @Override
    public void update(Observable o, Object arg) {
        DecisionVariable variable = (DecisionVariable) o;
        int currentValueOfVariable = variable.getValue();
        int previousValueOfVariable = getVariableValues().get(variable.getName());

        if (currentValueOfVariable == 1 && previousValueOfVariable == 0) {
            objectiveValue += getCoefficients().get(variable.getName());
        } else if (currentValueOfVariable == 0 && previousValueOfVariable == 1) {
            objectiveValue -= getCoefficients().get(variable.getName());
        }
        getVariableValues().put(variable.getName(), variable.getValue());
    }


    public void setFeasibilityFactor(double feasibilityFactor) {
        this.feasibilityFactor = feasibilityFactor;
    }

    public void updateFeasibilityFactor(double differenceInFeasibility) {
        feasibilityFactor += differenceInFeasibility;
    }
}
