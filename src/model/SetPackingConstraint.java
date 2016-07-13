package model;

import main.Main;

import java.util.*;
import java.util.Map.Entry;

public class SetPackingConstraint extends Constraint {

    private List<Entry<String, Double>> orderedcoefficients;
    private int conflictingVariablesBound;
    private Map<Long, List<String>> groups;
    private Set<Long> groupHashes;

    public SetPackingConstraint(String name, int type) {
        super(name, type);
        orderedcoefficients = new ArrayList<>();
        groups = new HashMap<>();
        groupHashes = new HashSet<>();
    }

    // Order coefficients in decending order
    public void orderCoefficients() {
        orderedcoefficients = new LinkedList<>(getCoefficients().entrySet());

        Collections.sort(orderedcoefficients, new Comparator<Entry<String, Double>>() {

            @Override
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
    }

    public void searchConflcitingPairs(Map<String, DecisionVariable> decisionVariables) {
        int i = 0;
        int j = 1;
        conflictingVariablesBound = orderedcoefficients.size();
        double coefficient1;
        double coefficient2;
        while (i < conflictingVariablesBound && j < orderedcoefficients.size()) {

            coefficient1 = orderedcoefficients.get(i).getValue();
            coefficient2 = orderedcoefficients.get(j).getValue();

            if (!checkIfFeasible(coefficient1 + coefficient2)) {
                decisionVariables.get(orderedcoefficients.get(i).getKey())
                        .addConflictingVariable(orderedcoefficients.get(j).getKey());
                decisionVariables.get(orderedcoefficients.get(j).getKey())
                        .addConflictingVariable(orderedcoefficients.get(i).getKey());
                j++;
                if (j >= orderedcoefficients.size()) {
                    i++;
                    j = i + 1;
                }
            } else {
                // System.err.println("BOUND UPDATE!!!!\n\n\n\n\n\n\n\n");
                conflictingVariablesBound = j;
                i++;
                j = i + 1;
            }
        }
    }


    /**
     * Group kan verwijdered worden als:
     * Een conflict is tussen twee variabelen in de group
     * Een onderling conflict is tussen een variabele of group en de de rest van de group.
     *
     * @param decisionvariables
     */
    private void cleanUpGroups(Map<String, DecisionVariable> decisionvariables, Map<Long, List<String>> tempGroups, Map<Long, ConflictingGroup> conflictingGroups) {

        int j;
        boolean conflict;
        List<String> group;
        Set<Long> groupsToRemove = new HashSet<>();
        Iterator it;
        DecisionVariable variable;
        ConflictingGroup conflictingGroup;
        for (Long hash : tempGroups.keySet()) {
            j = 0;
            conflict = false;
            group = tempGroups.get(hash);
            // Controle op conflict tussen twee variabelen in de group.
            while (j < group.size() - 1 && !conflict) {
                String variableName = group.get(j);
                int k = j + 1;
                while (k < group.size() && !conflict) {
                    if (decisionvariables.get(group.get(k)).getConflictingDecisionVariables().contains(variableName)) {
                        conflict = true;
                        groupsToRemove.add(hash);
                    }
                    k++;
                }
                j++;
            }
            it = decisionvariables.entrySet().iterator();
            while (it.hasNext() && !conflict) {
                variable = (DecisionVariable) ((Map.Entry) it.next()).getValue();
                if (variable.checkGroupsIsOverlapping(hash)) {
                    groupsToRemove.add(hash);
                    conflict = true;
                }
            }
            it = conflictingGroups.keySet().iterator();
            while (it.hasNext() && !conflict) {
                conflictingGroup = conflictingGroups.get(it.next());
                if (conflictingGroup.checkGroupsIsOverlapping(hash)) {
                    groupsToRemove.add(hash);
                    conflict = true;
                }
            }
        }
        for (Long hash : groupsToRemove) {
            tempGroups.remove(hash);
        }

    }

    private void constructGroups(int groupSize, Map<Long, List<String>> tempGroups) {

        String[] variables = new String[getCoefficients().size()];
        getCoefficients().keySet().toArray(variables);

        String[] combination = new String[groupSize];
        createGroups(variables, combination, 0, variables.length - 1, 0, groupSize, tempGroups);

    }

    public void searchConflitsBetweenGroupsAndVariables(Map<String, DecisionVariable> decisionvariables, Map<Long, ConflictingGroup> conflictingGroups) {


        List<Entry<Long, Double>> groupConstraintValueList = new ArrayList<>();

        Map<Long, List<String>> tempGroups;

        // System.out.println(Main.getTimeStamp() + "\t\tGenerating groups...");

        double constraintValue;
        int i;
        ConflictingGroup conflictingGroup;

        for (int groupSize = 2; groupSize <= Main.GROUPSIZE; groupSize++) {

            // Generation of groups.
            tempGroups = new HashMap<>();
            constructGroups(groupSize, tempGroups);

            // System.out.println(Main.getTimeStamp() + "\t\tGroups are generated, now clean up starts.");

            cleanUpGroups(decisionvariables, tempGroups, conflictingGroups);

            // System.out.println(Main.getTimeStamp() + "\t\tClean up of groups done");

            // System.out.println(Main.getTimeStamp() + "\t\tSearching for conflicts between decision variables and groups.");

            for (Entry<Long, List<String>> group : tempGroups.entrySet()) {

                constraintValue = 0;
                i = 0;
                // Berekenen van de waarde van de groep
                for (String variable : group.getValue()) {
                    constraintValue += getCoefficients().get(variable);
                }

                if (checkIfFeasible(constraintValue)) {
                    groupConstraintValueList.add(new AbstractMap.SimpleEntry<>(group.getKey(), constraintValue));

                    String currentDecisionVariablesName;
                    double coefficientCurrentVariable = orderedcoefficients.get(i).getValue();
                    while (!checkIfFeasible(constraintValue + coefficientCurrentVariable) && i < orderedcoefficients.size()) {

                        currentDecisionVariablesName = orderedcoefficients.get(i).getKey();
                        coefficientCurrentVariable = orderedcoefficients.get(i).getValue();

                        if (!group.getValue().contains(currentDecisionVariablesName)) {

                            // adds a link of the current conflicting group to the decisionvariable
                            decisionvariables.get(currentDecisionVariablesName).addConflictingGroup(group.getKey());

                            if (!conflictingGroups.containsKey(group.getKey())) {
                                conflictingGroup = new ConflictingGroup(group.getKey(), group.getValue());
                                conflictingGroup.addConstraintValue(getName(), constraintValue);
                                conflictingGroups.put(conflictingGroup.getHash(), conflictingGroup);
                            } else {
                                conflictingGroup = conflictingGroups.get(group.getKey());
                            }
                            conflictingGroup.addConflictingVariable(currentDecisionVariablesName);
                        }
                        i++;
                    }
                }
            }

            if (!groupConstraintValueList.isEmpty()) {

                // System.out.println(Main.getTimeStamp() + "\t\tSearching conflicts between groups");

                // Sort the groups in assending order based on their constraint value.
                Collections.sort(groupConstraintValueList, (o1, o2) -> {
                    if (o1.getValue() < o2.getValue()) return 1;
                    if (o1.getValue() > o2.getValue()) return -1;
                    else return 0;
                });

                searchConflictBetweenGroups(groupConstraintValueList, conflictingGroups, tempGroups);
            }

        }
    }

    private void searchConflictBetweenGroups(List<Entry<Long, Double>> orderedGroups, Map<Long, ConflictingGroup> conflictringGroups, Map<Long, List<String>> tempGroups) {
        int i = 0;
        int j = 1;
        int bound = orderedGroups.size();
        double constraintValue1;
        double constraintValue2;
        long hashGroup1;
        long hashGroup2;
        ConflictingGroup group1, group2;
        while (i < bound && j < orderedGroups.size()) {

            constraintValue1 = orderedGroups.get(i).getValue();
            constraintValue2 = orderedGroups.get(j).getValue();

            hashGroup1 = orderedGroups.get(i).getKey();
            hashGroup2 = orderedGroups.get(j).getKey();

            if (!checkIfFeasible(constraintValue1 + constraintValue2)) {

                if (!conflictringGroups.containsKey(hashGroup1)) {
                    group1 = new ConflictingGroup(hashGroup1, groups.get(hashGroup1));
                    group1.addConstraintValue(getName(), constraintValue1);
                    conflictringGroups.put(group1.getHash(), group1);
                } else {
                    group1 = conflictringGroups.get(hashGroup1);
                }

                if (!conflictringGroups.containsKey(hashGroup2)) {
                    group2 = new ConflictingGroup(hashGroup2, groups.get(hashGroup2));
                    group2.addConstraintValue(getName(), constraintValue2);
                    conflictringGroups.put(group2.getHash(), group2);
                } else {
                    group2 = conflictringGroups.get(hashGroup2);
                }

                group1.addConflictingGroup(hashGroup2);
                group2.addConflictingGroup(hashGroup1);

                j++;
                if (j >= orderedGroups.size()) {
                    i++;
                    j = i + 1;
                }
            } else {
                bound = j;
                i++;
                j = i + 1;
            }
        }
    }

    /**
     * variables[]  ---> variables in de constraint
     * combination[] ---> Temporary array to store current combination
     * List<String[]> ---> List of all the combinations made
     * start & end ---> Staring and Ending indexes in variables[]
     * index  ---> Current index in combination[]
     * r ---> Size of a combination to be made
     */
    private void createGroups(String[] variables, String[] combination, int start,
                              int end, int currentPositionOfCombination, int groupSize, Map<Long, List<String>> tempGroups) {
        // Current combination is done
        if (currentPositionOfCombination == groupSize) {
            long hash = 0;
            for (String string : combination) {
                hash += string.hashCode();
            }
            if (!groupHashes.contains(hash)) {
                tempGroups.put(hash, new ArrayList<>(Arrays.asList(combination)));
                groups.put(hash, new ArrayList<>(Arrays.asList(combination)));
                groupHashes.add(hash);
            }
            return;
        }

        for (int i = start; i <= end && end - i + 1 >= groupSize - currentPositionOfCombination; i++) {

            combination[currentPositionOfCombination] = variables[i];
            createGroups(variables, combination, i + 1, end, currentPositionOfCombination + 1, groupSize, tempGroups);
        }
    }

    Set<String> searchForbiddenVariables() {
        Set<String> forbiddenVariables = new HashSet<>();
        boolean search = true;
        int i = 0;
        while (search && i < orderedcoefficients.size()) {
            if (orderedcoefficients.get(i).getValue() > getRightHandSide()) {
                i++;
                forbiddenVariables.add(orderedcoefficients.get(i).getKey());
            } else {
                search = false;
            }

        }
        return forbiddenVariables;
    }

    /**
     * Checks if the creation of groups is possible. This means that there are coefficients greater then 1 in the {@link Constraint}.
     *
     * @return True is creation is groups is possible, false if not.
     */
    public boolean groupsPossible() {
        double combinedCoefficient = 0;
        for (Double coefficient : getCoefficients().values()) {
            combinedCoefficient += coefficient;
        }
        return !(combinedCoefficient == getCoefficients().size()) || !(getRightHandSide() == 1);
    }
}
