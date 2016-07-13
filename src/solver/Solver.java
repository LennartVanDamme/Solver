package solver;

import exceptions.BPException;
import io.OutputWriter;
import io.Reader;
import model.*;
import operators.*;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class Solver {

    private Model model;
    private Heuristic heuristic;
    private Solution solution;
    private Reader reader;
    private OutputWriter outputWriter;
    private SimpleGraph<DecisionVariable, DefaultEdge> conflictingGraph;
    private Map<Long, ConflictingGroup> conflictingGroups;
    private Map<Integer, Clique> cliques;
    private long totalSearchTimeConflict;

    /**
     * Create an instance of the {@link Solver} class.
     *
     * @param reader                    {@link Reader} specified by the user. The user can implement its own {@link Reader} instance. When no {@link Reader} is specified a {@link BPException} will be thrown.
     * @param minimize                  Boolean to speficied if the solver should minimize the {@link ObjectiveFunction}. When true is given, the {@link Solver} will minimize, others wise it will maximize.
     * @param lateAcceptanceFitnessSize Specifies the size of the fitness array in the {@link Heuristic}.
     * @param crossConstraintParam      Specifies the maximum amount of variables to flip in the same constraint for the {@link CrossConstraintOperator}. When 0 is given, no instance is made of {@link CrossConstraintOperator}.
     * @param conflictGraphDepth        Specifies the depth of search for the {@link GraphOperator}. When 0 is given, no instance is made of {@link GraphOperator}.
     * @param orderedCyclicShift        When true is given as parameter, an instance of {@link OrderedCyclicShiftOperator} is created for the {@link Heuristic} to use.
     * @param unorderedCyclicShift      When true is given as parameter, an instance of {@link UnorderedCyclicShiftOperator} is created for the {@link Heuristic} to use.
     * @throws BPException An {@link BPException} is thrown when no {@link Reader} is specified by the user.
     */
    public Solver(Reader reader, boolean minimize, int lateAcceptanceFitnessSize, int crossConstraintParam, int conflictGraphDepth, int orderedCyclicShift, int unorderedCyclicShift) throws BPException {
        model = new Model();
        if (reader == null) {
            throw new BPException("No reader specified. Specify a reader");
        } else {
            this.reader = reader;
        }
        heuristic = new Heuristic(minimize, lateAcceptanceFitnessSize);
        solution = new Solution(model);
        outputWriter = new OutputWriter();
        cliques = new HashMap<>();
        conflictingGroups = new HashMap<>();
        createOperators(crossConstraintParam, conflictGraphDepth, orderedCyclicShift, unorderedCyclicShift);
    }

    /**
     * @param type String representing the type of constraint
     * @return An integer value representing the type of Constraint. 0 for a
     * non-restrictive constraint, 1 for a set covering constraint, 2
     * for a set packing constraint and 3 for an equility constraint
     */

    public static int constraintTypeConverter(String type) {
        if (type.toUpperCase().equals("N"))
            return Constraint.NON_RESTRICTIVE;
        else if (type.toUpperCase().equals("G"))
            return Constraint.SET_COVERING_CONSTRAINT;
        else if (type.toUpperCase().equals("L"))
            return Constraint.SET_PACKING_CONSTRAINT;
        else
            return Constraint.EQUALITY;
    }

    /**
     * Creates the operators the user wants.
     *
     * @param crossConstraintParam Maximum amount of variables to flip in the same constraint for the {@link CrossConstraintOperator}. When 0 is given, no instance is made of {@link CrossConstraintOperator}.
     * @param conflictGraphDepth   Depth of search for the {@link GraphOperator}. When 0 is given, no instance is made of {@link GraphOperator}.
     * @param orderedCyclicShift   When true is given as parameter, an instance of {@link OrderedCyclicShiftOperator} is created.
     * @param unorderedCyclicShift When true is given as parameter, an instance of {@link UnorderedCyclicShiftOperator} is created.
     */
    private void createOperators(int crossConstraintParam, int conflictGraphDepth, int orderedCyclicShift, int unorderedCyclicShift) {
        if (crossConstraintParam != 0) {
            heuristic.addOperator(new CrossConstraintOperator(this, crossConstraintParam));
        }
        if (conflictGraphDepth != 0) {
            heuristic.addOperator(new GraphOperator(this, conflictGraphDepth));
        }
        if (orderedCyclicShift != 0) {
            heuristic.addOperator(new OrderedCyclicShiftOperator(this));
        }
        if (unorderedCyclicShift != 0) {
            heuristic.addOperator(new UnorderedCyclicShiftOperator(this));
        }
        heuristic.addOperator(new FlipOperator(this));
    }

    /**
     * @return Used heuristic object.
     */
    public Heuristic getHeuristic() {
        return heuristic;
    }

    public Solution getSolution() {
        return solution;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * Start generating solutions and enhancing them using a late acceptance
     * meta-heuristic
     *
     * @param runTime The amount of run time allowed by the user.
     */
    public void solve(long runTime) throws Exception, Error {
        solution.initializeDecisionvariableList();
        model.checkForForbiddenVariables();
        solution.excludeForbiddenVariables(model.getForbiddenVariables());
        long startSearchConflict = System.currentTimeMillis();
        //searchConflicts();
        totalSearchTimeConflict = System.currentTimeMillis() - startSearchConflict;
        //createConflictGraph();
        //System.out.println(Main.getTimeStamp() + "\tStart clique detection...");
        //detectCliques();
        //System.out.println(Main.getTimeStamp() + "\tClique detection finished.");
        //initiateConflictingGroups();
        setObserverStructure();
        model.getObjectiveFunction().calculatePenaltyCost();
        calculateInitialState();
        heuristic.setRunTime(runTime);
        System.out.println();
        heuristic.start(solution, model);
        System.out.println();
    }

    private void initiateConflictingGroups() {
        for (ConflictingGroup group : conflictingGroups.values()) {
            for (String variableName : group.getToObserveDecisionVariables()) {
                group.addVariableToGroup(solution.getDecisionvariable(variableName));
            }
        }
    }

    private void calculateInitialState() {
        double totalAmountOfConstraintDelta = 0;
        double constraintDelta;
        for (Constraint constraint : model.getConstraints().values()) {
            constraint.calculateValue();
            constraint.checkIfFeasible();
            constraint.updateConstraintDelta();
            constraintDelta = constraint.getConstraintDelta();
            totalAmountOfConstraintDelta += constraintDelta;
            model.addConstraintDelta(constraint.getName(), constraintDelta);
            if (!constraint.isFeasible()) {
                model.getInfeasibleConstraints().add(constraint);
            }
        }
        model.setTotalConstraintDelta(totalAmountOfConstraintDelta);
        model.getObjectiveFunction().setFeasibilityFactor(totalAmountOfConstraintDelta);
        model.getObjectiveFunction().calculateObjectivevalue();
    }

    private void setObserverStructure() throws Exception {

        /**
         * Setup obsever pattern for the constraints so that changes in decisionvariables get noticed automatically by the constraints
         * */

        for (DecisionVariable decisionVariable : solution.getDecisionVariableMap().values()) {
            for (String constraintName : decisionVariable.getConstraints()) {
                decisionVariable.addObserver(model.getContraint(constraintName));
            }
            if (decisionVariable.isInObjective()) {
                decisionVariable.addObserver(model.getObjectiveFunction());
            }
        }

        /**
         * Enables the model to keep a list of infeasible constraints using the observer pattern
         * */

        model.setObserverStructure();

        /**
         * Toevoegen van de conflicterende groepen en cliques als observer bij beslissingsvariabelen
         * */

        for (ConflictingGroup group : conflictingGroups.values()) {
            for (String variableToObserve : group.getToObserveDecisionVariables()) {
                solution.getDecisionvariable(variableToObserve).addObserver(group);
            }
        }

        for (DecisionVariable variable : solution.getDecisionVariableMap().values()) {
            for (Integer cliqueID : variable.getCliquesContainingVariable()) {
                variable.addObserver(cliques.get(cliqueID));
            }
        }

    }

    private void searchConflicts() throws Exception, Error {
        SetPackingConstraint setPackingConstraint;
        for (String setPackingConstraintName : model.getSetPackingConstraints()) {
            setPackingConstraint = (SetPackingConstraint) model.getContraint(setPackingConstraintName);
            setPackingConstraint.orderCoefficients();
            setPackingConstraint.searchConflcitingPairs(solution.getDecisionVariableMap());
        }
        double i = 1.0;
        final double totalNumberPackingConstraints = model.getSetPackingConstraints().size();
        for (String setPackingConstraintName : model.getSetPackingConstraints()) {

            setPackingConstraint = (SetPackingConstraint) model.getContraint(setPackingConstraintName);

            if (setPackingConstraint.groupsPossible()) {
                // System.out.println(Main.getTimeStamp() + "\t\tStart search of conflicting groups.");
                setPackingConstraint.searchConflitsBetweenGroupsAndVariables(solution.getDecisionVariableMap(), conflictingGroups);
                // System.out.println(Main.getTimeStamp() + "\t\tSearch for conflicting groups is completed.");
            }
            i++;

        }
    }

    private void createConflictGraph() {


        conflictingGraph = new SimpleGraph<>(DefaultEdge.class);
        // Adding all the decisionvariables
        for (DecisionVariable decisionVariable : solution.getDecisionVariableMap().values()) {
            conflictingGraph.addVertex(decisionVariable);
        }
        // Adding all the conflictingGroupDecisionVariables
        for (ConflictingGroup conflictingGroup : conflictingGroups.values()) {
            conflictingGraph.addVertex(conflictingGroup);
        }

        // System.out.println(conflictingGraph.toString());

        /**Creating the edges in the graph.
         * For conflicts with other DecisionVariables a one way edge is enough
         * since we go through all de variables, so the other one will create an
         * edge as well. For the conflictingGroups we have to make edges in
         * both directions.
         * */

        for (DecisionVariable variable : solution.getDecisionVariableMap().values()) {
            for (String conflictingName : variable.getConflictingDecisionVariables()) {
                if (!conflictingGraph.containsEdge(variable, solution.getDecisionvariable(conflictingName)) &&
                        !conflictingGraph.containsEdge(solution.getDecisionvariable(conflictingName), variable))
                    conflictingGraph.addEdge(variable, solution.getDecisionVariableMap().get(conflictingName));
            }
            for (Long hash : variable.getConflictingGroups()) {
                conflictingGraph.addEdge(variable, conflictingGroups.get(hash));
            }
        }
        for (ConflictingGroup group : conflictingGroups.values()) {
            for (Long hash : group.getConflictingGroups()) {
                if (!conflictingGraph.containsEdge(group, conflictingGroups.get(hash)) && !conflictingGraph.containsEdge(conflictingGroups.get(hash), group))
                    conflictingGraph.addEdge(group, conflictingGroups.get(hash));
            }
        }

    }

    private void detectCliques() {
        BronKerboschCliqueFinder<DecisionVariable, DefaultEdge> bronKerboschCliqueFinder = new BronKerboschCliqueFinder<>(
                conflictingGraph);
        List<Set<DecisionVariable>> cliqueList = new ArrayList<>(bronKerboschCliqueFinder.getAllMaximalCliques());
        for (int i = 0; i < cliqueList.size(); i++) {
            Clique clique = new Clique(i, cliqueList.get(i));
            clique.connectDecisionVariables();
            cliques.put(i, clique);
        }

    }

    public void printSolution(String path) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
            writer.write("Solution for problem: " + model.getName() + "\n");
            writer.write("Total time for conflict search: " + totalSearchTimeConflict + "\n");
            writer.write("Total amount of conflicting groups: " + conflictingGroups.size() + "\n");
            if (model.getInfeasibleConstraints().isEmpty()) {
                writer.write("Solution is feasible.\n");
            } else {
                writer.write("Solution is infeasible and has " + model.getInfeasibleConstraints().size()
                        + " violations to constraints.\n");
                writer.write("The violated constraints are: \n");
                for (Constraint violatedConstraint : model.getInfeasibleConstraints()) {
                    writer.write("\t" + violatedConstraint.getName() + "\n");
                }
            }
            writer.write("\nObjectiveFunction value: " + solution.getObjectiveValue() + "\n\n");
            writer.write("Number of moves evaluated: " + heuristic.nSteps + "; Number of moves done: "
                    + heuristic.nMoveAccepted + "\n");
            writer.write("Values of de decision variables:\n");
            for (DecisionVariable variable : solution.getDecisionVariableMap().values()) {
                if (variable.getValue() == 1) {
                    writer.write(variable.toString() + "\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void readProblem(String filepath) {
        reader.readFile(filepath, this);
    }

    public void readProblem(File inputFile) {
        reader.readFile(inputFile.getAbsolutePath(), this);
    }

    public void printConstraints() {
        try {
            outputWriter.writeConstraints(model.prepCosntraintsForWriting());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printDecisionvariables() {
        try {
            outputWriter.writeDecisionvariables(solution.prepDecisionvariablesForWriting());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public PriorityQueue<Constraint> getViolatedConstraints() {
        return model.getInfeasibleConstraints();
    }

    public Constraint getViolatedConstraint() {
        if (model.getInfeasibleConstraints().isEmpty()) return null;
        else return model.getInfeasibleConstraint();
    }

    public DecisionVariable getDecisionVaraible(String decionvariableName) {
        return solution.getDecisionvariable(decionvariableName);
    }

    public Constraint getViolatedConstraint(String variableName) {
        if (model.getInfeasibleConstraints().isEmpty()) return null;
        Set<String> constraints = getDecisionVaraible(variableName).getConstraints();
        for (Constraint constraint : model.getInfeasibleConstraints()) {
            if (constraints.contains(constraint.getName())) return constraint;
        }
        return null;
    }

    public Constraint getRandomConstraint() {
        return model.getRandomConstraint();
    }
}
