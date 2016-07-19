package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Solution {
    private Map<String, DecisionVariable> decisionVariableMap;
    private List<DecisionVariable> decisionVariableList;
    private Model model;

    public Solution(Model model) {
        decisionVariableMap = new HashMap<>();
        decisionVariableList = new ArrayList<>();
        this.model = model;
    }

    public void initializeDecisionvariableList() {
        decisionVariableList.addAll(decisionVariableMap.values());
    }

    public int getnDecisionVariabels() {
        return decisionVariableList.size();
    }

    public Map<String, DecisionVariable> getDecisionVariableMap() {
        return decisionVariableMap;
    }

    public double getObjective() {
        return model.getObjectiveFunction().getTotalValue();
    }

    public double getObjectiveValue() {
        return model.getObjectiveFunction().getObjectiveValue();
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public void printSolution(String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(new File("doc/toyProblems/solutions/" + model.getName() + "_" + fileName)));
            writer.write("ObjectiveFunction: " + model.getObjectiveFunction().getObjectiveValue() + "\n");
            for (DecisionVariable variable : decisionVariableMap.values()) {
                writer.write(variable.toString());
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String[] prepDecisionvariablesForWriting() {
        String[] decisionvariables = new String[decisionVariableMap.size()];
        int i = 0;
        for (DecisionVariable decisionVariable : decisionVariableMap.values()) {
            decisionvariables[i] = decisionVariable.toString();
            i++;
        }
        return decisionvariables;
    }

    public DecisionVariable getDecisionvariable(String decisionvariableName) {
        return decisionVariableMap.get(decisionvariableName);
    }

    // TODO: Implement method. Used in CrossConstraintOperator.
    public List<DecisionVariable> giveRandomVariables(int amountOfVariablesSelected) {
        return null;
    }

    public void flipVariable(String decisionVariableName) {
        decisionVariableMap.get(decisionVariableName).flipValue();
    }

    public String giveRandomVariable() {
        return decisionVariableList.get(Heuristic.RANDOM.nextInt(decisionVariableList.size())).getName();
    }

    public void excludeForbiddenVariables(Set<String> forbiddenVariables) {
        for (String forbiddenVariable : forbiddenVariables) {
            decisionVariableMap.remove(forbiddenVariable);
            decisionVariableList.remove(forbiddenVariable);
        }
    }

    public int [] variableValues(){
        int [] variablesValues = new int [decisionVariableList.size()];
        int i = 0;
        for(DecisionVariable variable : decisionVariableList){
            variablesValues[i] = variable.getValue();
            i++;
        }
        return variablesValues;
    }

    public void setBestFoundSolution(int[] bestSolution) {
        for(int i = 0; i<bestSolution.length; i++){
            if (bestSolution[i] != decisionVariableList.get(i).getValue()){
                decisionVariableList.get(i).flipValue();
            }
        }
    }

    public String flipRandomVariable() {

        return null;
    }
}
