package core;

import au.com.bytecode.opencsv.CSVWriter;
import main.Main;
import core.Model;
import core.Solution;
import main.Main2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;

/**
 * Created by Lennart on 11/07/16.
 */
public class HeuristicLog extends TimerTask {

    private Solution solution;
    private Model model;
    private long startTime;
    private CSVWriter csvWriter;

    HeuristicLog(Solution solution, Model model, long startTime) throws IOException {
        this.solution = solution;
        this.model = model;
        this.startTime = startTime;
        csvWriter = new CSVWriter(new FileWriter(Main2.outputPath + "objValues/" + model.getName() + ".csv"), ',');
        csvWriter.writeNext("Time", "Kost huidige oplossing", "nViolations huidige oplossing",
                "Feasability factor huidige oplossing");
    }

    // Time
    // Kost huidige oplossing
    // Kost beste oplossing
    // nViolations huidige oplossing
    // nViolations beste oplossing
    // Feasability factor huidige oplossing
    // Feasibility beste oplossing
    public void run() {
        long time = (System.currentTimeMillis() / 1000) - this.startTime;
        csvWriter.writeNext("" + time, "" + solution.getObjective(), "" + model.getInfeasibleConstraints().size(),
                "" + model.getObjectiveFunction().getFeasibilityFactor());
    }

    public void endWrite() throws IOException {
        csvWriter.close();
    }
}
