package core;

import au.com.bytecode.opencsv.CSVWriter;
import main.Main;
import main.Main2;
import operators.Operator;

import java.io.FileWriter;
import java.util.*;

public class Heuristic {

    public static final Random RANDOM = new Random(0);
    static boolean MINIMIZE;
    private final int L;
    int nSteps;
    int nMoveAccepted;
    private double[] fitness;
    private List<Operator> operators;
    private long runTime;
    private double bestObjectiveValue;
    private int [] bestSolution;
    private int nViolationsBestSolution;
    private double feasibilityFactorBestSolution;
    private List<Long> timeNewBestSolutionFound;
    private CSVWriter csvWriter;

    public Heuristic(boolean minimize, int lateAcceptanceSize) {
        Heuristic.MINIMIZE = minimize;
        L = lateAcceptanceSize;
        fitness = new double[L];
        operators = new ArrayList<>();
        timeNewBestSolutionFound = new ArrayList<>();
        nSteps = 0;
        bestObjectiveValue = 0;
    }

    void addOperator(Operator operator) {
        operators.add(operator);
    }

    void setRunTime(long runTime) {
        this.runTime = runTime;
    }

    public void start(Solution solution, Model model) throws Exception {
        csvWriter = new CSVWriter(new FileWriter(Main2.outputPath + "objValues/" + model.getName() + "(bestSolution).csv"), ',');
        csvWriter.writeNext("Tijd", "Kost beste oplossing", "nViolations beste oplossing", "Feasibility factor beste oplossing");
        bestObjectiveValue = solution.getObjective();
        bestSolution = solution.variableValues();
        nViolationsBestSolution = model.getInfeasibleConstraints().size();
        feasibilityFactorBestSolution = model.getObjectiveFunction().getFeasibilityFactor();

        initiateFitnessArray(bestObjectiveValue);

        Operator operator;
        long startTime = System.currentTimeMillis() / 1000;
        long currentTime = System.currentTimeMillis() / 1000;

        Timer timer = new Timer();
        HeuristicLog heuristicLog = new HeuristicLog(solution, model, startTime);
        timer.schedule(heuristicLog, 0, Main.LOG_PERIOD);
        csvWriter.writeNext("" + (System.currentTimeMillis() - startTime), "" + bestObjectiveValue, "" + nViolationsBestSolution, "" + feasibilityFactorBestSolution);
        double oldObjectiveValue, newObjectiveValue;

        int v; // Is de counter die over de fitness array loopt.
        while (runTime == -1 || (currentTime - startTime) < runTime) {
            currentTime = System.currentTimeMillis() / 1000;
            v = nSteps % L;
            
            operator = operators.get(RANDOM.nextInt(operators.size()));
            System.out.println(operator);
            oldObjectiveValue = solution.getObjective();
            operator.doMove(solution);

            newObjectiveValue = solution.getObjective();
            if (!acceptMove(v, newObjectiveValue, oldObjectiveValue)) {
                operator.undoMove(solution);
            }
            fitness[v] = solution.getObjective();

            // Update of best know results
            if (MINIMIZE && fitness[v] < bestObjectiveValue) {
                bestObjectiveValue = fitness[v];
                bestSolution = solution.variableValues();
                nViolationsBestSolution = model.getInfeasibleConstraints().size();
                feasibilityFactorBestSolution = model.getObjectiveFunction().getFeasibilityFactor();
                timeNewBestSolutionFound.add(System.currentTimeMillis()-startTime);
                csvWriter.writeNext("" + (System.currentTimeMillis()-startTime), "" + bestObjectiveValue, "" + nViolationsBestSolution, ""+ feasibilityFactorBestSolution);
            } else if (!MINIMIZE && fitness[v] > bestObjectiveValue) {
                bestObjectiveValue = fitness[v];
                bestSolution = solution.variableValues();
                nViolationsBestSolution = model.getInfeasibleConstraints().size();
                feasibilityFactorBestSolution = model.getObjectiveFunction().getFeasibilityFactor();
                timeNewBestSolutionFound.add(System.currentTimeMillis()-startTime);
                csvWriter.writeNext("" + (System.currentTimeMillis()-startTime), "" + bestObjectiveValue, "" + nViolationsBestSolution, ""+ feasibilityFactorBestSolution);
            }
            nSteps++;
        }
        heuristicLog.endWrite();
        timer.cancel();
        csvWriter.close();
    }

    /**
     * Fills up the fitness array with the initial objective value of the {@link Solution}.
     *
     * @param objectiveValue Initial objective value.
     */
    private void initiateFitnessArray(double objectiveValue) {
        Arrays.fill(fitness, objectiveValue);
    }

    /**
     * Checks if a move can be accepted. When minimizing the objective, the newObjectiveValue must be smaller then the value in the fitness array in this iteration
     * or smaller then the oldObjectiveValue. When maximizing the objective, the newObjectiveValue must be greater then the value in the fitness array in this
     * iteration or greater then the oldObjectiveValue.
     *
     * @param v                 Current position in the fitness array.
     * @param newObjectiveValue The new objective value.
     * @param oldObjectiveValue The old objective value.
     * @return Returns true when the move can be accepted, false if not.
     */
    private boolean acceptMove(int v, double newObjectiveValue, double oldObjectiveValue) {
        if (MINIMIZE) {
            return ((newObjectiveValue <= fitness[v]) || (newObjectiveValue <= oldObjectiveValue));
        } else {
            return ((newObjectiveValue >= fitness[v]) || (newObjectiveValue >= oldObjectiveValue));
        }
    }

    public double getBestObjectiveValue() {
        return bestObjectiveValue;
    }

    public int[] getBestSolution() {
        return bestSolution;
    }

    public int getnViolationsBestSolution() {
        return nViolationsBestSolution;
    }

    public double getFeasibilityFactorBestSolution() {
        return feasibilityFactorBestSolution;
    }

    public int getnSteps() {
        return nSteps;
    }

    public List<Long> getTimeNewBestSolutionFound() {
        return timeNewBestSolutionFound;
    }
}
