package solver;

import main.Main;
import model.Model;
import model.Solution;
import operators.Operator;

import java.util.*;

public class Heuristic {

    public static final Random RANDOM = new Random(0);
    public static boolean MINIMIZE;
    private final int L;
    public int nSteps;
    public int nMoveAccepted;
    private double[] fitness;
    private List<Operator> operators;
    private long runTime;
    private double bestObjectiveValue;

    public Heuristic(boolean minimize, int lateAcceptanceSize) {
        Heuristic.MINIMIZE = minimize;
        L = lateAcceptanceSize;
        fitness = new double[L];
        operators = new ArrayList<>();
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

        bestObjectiveValue = solution.getObjective();

        initiateFitnessArray(bestObjectiveValue);

        Operator operator;
        long startTime = System.currentTimeMillis() / 1000;
        long currentTime = System.currentTimeMillis() / 1000;

        Timer timer = new Timer();
        timer.schedule(new ObjectiveThread(solution, model, startTime), 0, Main.LOG_PERIOD);

        System.out.println("test");

        double oldObjectiveValue, newObjectiveValue;
        int v; // Is de counter die over de fitness array loopt.
        while (runTime == -1 || (currentTime - startTime) < runTime) {
            currentTime = System.currentTimeMillis() / 1000;
            v = nSteps % L;
            if (v == 0) System.out.println("Complete run");
            operator = operators.get(RANDOM.nextInt(operators.size()));
            // System.out.println(operator);
            oldObjectiveValue = solution.getObjective();
            operator.doMove(solution);

            newObjectiveValue = solution.getObjective();
            if (!acceptMove(v, newObjectiveValue, oldObjectiveValue)) {
                operator.undoMove(solution);
            }
            fitness[v] = solution.getObjective();
//            if (MINIMIZE && fitness[v] < bestObjectiveValue) {
//                bestObjectiveValue = fitness[v];
//                System.out.println(Main.getTimeStamp() + "\tNew best objective value found: " + bestObjectiveValue);
//            } else if (!MINIMIZE && fitness[v] > bestObjectiveValue) {
//                bestObjectiveValue = fitness[v];
//                System.out.println(Main.getTimeStamp() + "\tNew best objective value found: " + bestObjectiveValue);
//            }
            nSteps++;
        }
        timer.cancel();
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

}
