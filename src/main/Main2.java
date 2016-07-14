package main;

import au.com.bytecode.opencsv.CSVWriter;
import io.MPSReader;
import solver.Solver;

import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Created by Lennart on 13/07/16.
 */
public class Main2 {

    public static final Logger LOGGER = Logger.getLogger("BP_Solver_Logger");
    public static final long LOG_PERIOD = 500;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static int GROUPSIZE = 2;
    public static String outputPath;

    private static FileHandler fh;

    public static void main(String[] args) throws Exception {

        final String inputFilePath = args[0];
        final boolean minimize = true;
        final long runtime = 10;
        final int lateAcceptanceFitnessSize = 10000;
        final int crossConstraintParam = 5;
        final int conflictGraphDepth = 0;
        final int orderedCyclicShift = 0;
        final int unorderedCyclicShift = 0;

        File inputFile = new File(inputFilePath);
        outputPath = args[1];

        Solver solver = solver = new Solver(new MPSReader(), minimize, lateAcceptanceFitnessSize, crossConstraintParam, conflictGraphDepth, orderedCyclicShift, unorderedCyclicShift);
        solver.readProblem(inputFile);

        solver.solve(runtime);
        if (solver.getViolatedConstraints().isEmpty()) {
            System.out.println(getTimeStamp() + "\tObjectiveValue = " + solver.getModel().getObjectiveFunction().getTotalValue());
        } else {
            System.out.println(getTimeStamp() + "\tSolution infeasible.");
        }

        solver.printSolution(outputPath + "/solutions/" + inputFile.getName().substring(0, inputFile.getName().length() - 4) + ".txt");


    }


    public static String getTimeStamp() {
        return "[" + SIMPLE_DATE_FORMAT.format(new Timestamp(Calendar.getInstance().getTime().getTime())) + "]";
    }
}
