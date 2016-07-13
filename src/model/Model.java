package model;

import solver.Heuristic;

import java.util.*;

public class Model implements Observer {

    private Map<String, Constraint> constraints;
    private List<String> setPackingConstraints;
    private String name;
    private ObjectiveFunction objectiveFunction;
    private PriorityQueue<Constraint> infeasibleConstraints;
    private Map<String, Double> constraintDeltas;
    private Set<String> forbiddenVariables;
    private List<String> constraintNames;
    private double totalConstraintDelta;

    public Model() {
        constraints = new HashMap<>();
        setPackingConstraints = new ArrayList<>();
        infeasibleConstraints = new PriorityQueue<>();
        forbiddenVariables = new HashSet<>();
        constraintDeltas = new HashMap<>();
        totalConstraintDelta = 0;
    }

    public ObjectiveFunction getObjectiveFunction() {
        return objectiveFunction;
    }

    public void setObjectiveFunction(ObjectiveFunction objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
    }

    public double getTotalConstraintDelta() {
        return totalConstraintDelta;
    }

    public void setTotalConstraintDelta(double totalConstraintDelta) {
        this.totalConstraintDelta = totalConstraintDelta;
    }

    public Set<String> getForbiddenVariables() {
        return forbiddenVariables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSetPackingConstraints() {
        return setPackingConstraints;
    }

    public void addConstraint(Constraint constraint) {
        constraints.put(constraint.getName(), constraint);
        if (constraint.getType() == Constraint.SET_PACKING_CONSTRAINT) {
            setPackingConstraints.add(constraint.getName());
        }
    }

    public boolean containsConstraint(String constraintName) {
        return constraints.containsKey(constraintName);
    }

    public void addSetPackingConstraint(String constraintName) {
        setPackingConstraints.add(constraintName);
    }

    public Constraint getContraint(String constraintName) {
        return constraints.get(constraintName);
    }

    public void addCoeffientToConstraint(String constraintName, String decisionVariableName, double coefficient) {
        constraints.get(constraintName).addCoefficient(decisionVariableName, coefficient);
    }

    public String[] prepCosntraintsForWriting() {
        String[] constraintArray = new String[constraints.size()];
        int i = 0;
        for (Constraint constraint : constraints.values()) {
            constraintArray[i] = constraint.toString();
            i++;
        }
        return constraintArray;
    }

    @Override
    public void update(Observable o, Object arg) {
        Constraint constraint = (Constraint) o;
        if ((int) arg == Constraint.BECAME_FEASIBLE) {
            infeasibleConstraints.remove(constraints.get(constraint.getName()));
        } else if ((int) arg == Constraint.BECAME_INFEASIBLE) {
            infeasibleConstraints.add(constraints.get(constraint.getName()));
        }
        double prevConstriantDelta = constraintDeltas.get(constraint.getName());
        double currentConstraintDelta = constraint.getConstraintDelta();
        totalConstraintDelta += (currentConstraintDelta - prevConstriantDelta);
        objectiveFunction.updateFeasibilityFactor(currentConstraintDelta - prevConstriantDelta);
        constraintDeltas.put(constraint.getName(), currentConstraintDelta);
    }

    public void setObserverStructure() {
        objectiveFunction.addObserver(this);
        for (Constraint constraint : constraints.values()) {
            constraint.addObserver(this);
        }
    }

    public Map<String, Constraint> getConstraints() {
        return constraints;
    }

    public void addConstraintDelta(String constraintName, double constraintDelta) {
        constraintDeltas.put(constraintName, constraintDelta);
    }

    public PriorityQueue<Constraint> getInfeasibleConstraints() {
        return infeasibleConstraints;
    }

    public void checkForForbiddenVariables() {
        for (String setpackingconstraintsName : setPackingConstraints) {
            SetPackingConstraint setPackingConstraint = (SetPackingConstraint) constraints.get(setpackingconstraintsName);
            forbiddenVariables.addAll(setPackingConstraint.searchForbiddenVariables());
        }
    }

    public Constraint getRandomConstraint() {
        if (constraintNames == null) constraintNames = new ArrayList<>(constraints.keySet());
        return constraints.get(constraintNames.get(Heuristic.RANDOM.nextInt(constraints.size())));
    }

    public Constraint getInfeasibleConstraint() {
        double rouletteValue = Heuristic.RANDOM.nextDouble() * totalConstraintDelta;
        Iterator<Constraint> it = infeasibleConstraints.iterator();
        Constraint constraint;
        while (it.hasNext()) {
            constraint = it.next();
            rouletteValue -= constraint.getConstraintDelta();
            if (rouletteValue <= 0) return constraint;
        }
        return infeasibleConstraints.peek();
    }
}
