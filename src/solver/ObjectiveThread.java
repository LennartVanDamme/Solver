package solver;

import main.Main;
import model.Model;
import model.Solution;

import java.util.TimerTask;

/**
 * Created by Lennart on 11/07/16.
 */
public class ObjectiveThread extends TimerTask {

    private Solution solution;
    private Model model;
    private long startTime;

    public ObjectiveThread(Solution solution, Model model, long startTime) {
        this.solution = solution;
        this.model = model;
        this.startTime = startTime;
    }

    public void run() {
        long time = (System.currentTimeMillis() / 1000) - this.startTime;
        Main.csvWriter.writeNext("" + time, "" + solution.getObjective(), "" + model.getInfeasibleConstraints().size(), "" + solution.getObjectiveValue());
    }
}
