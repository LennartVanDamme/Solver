package main;

import au.com.bytecode.opencsv.CSVWriter;
import io.MPSReader;
import solver.Solver;

import java.io.File;
import java.io.FileWriter;
import java.util.logging.FileHandler;

public class Main {

    public static final int GROUPSIZE = 2;
    public static CSVWriter csvWriter;
    public static long LOG_PERIOD = 1000;
    private static FileHandler fh;

    public static void main(String[] args) throws Exception {

        final boolean minimize = true;
        final long runtime = 20 * 60;
        int lateAcceptanceFitnessSize = 10000;
        int crossConstraintParam = 10;
        final int conflictGraphDepth = 0;
        final int orderedCyclicShift = 0;
        final int unorderedCyclicShift = 0;

        File directory = new File("doc/mps_Files");


        for (File mpsFile : directory.listFiles()) {

            Solver solver = solver = new Solver(new MPSReader(),
                    minimize, lateAcceptanceFitnessSize, crossConstraintParam, conflictGraphDepth,
                    orderedCyclicShift, unorderedCyclicShift);

            solver.readProblem(mpsFile);
            csvWriter = new CSVWriter(new FileWriter("doc/output/L=10000_K=10/objectiveValues/" +
                    mpsFile.getName().substring(0, mpsFile.getName().length() - 4) + ".csv"), '\t');
            csvWriter.writeNext("Time", "Total Objective value", "Number of violated constraints", "Objective value");
            solver.solve(runtime);
            solver.printSolution("doc/output/L=10000_K=10/solutions/" + mpsFile.getName().substring(0, mpsFile.getName().length() - 4) + ".txt");
            csvWriter.close();

            lateAcceptanceFitnessSize = 25000;

            solver = new Solver(new MPSReader(), minimize, lateAcceptanceFitnessSize, crossConstraintParam, conflictGraphDepth, orderedCyclicShift, unorderedCyclicShift);
            solver.readProblem(mpsFile);
            csvWriter = new CSVWriter(new FileWriter("doc/output/L=25000_K=10/objectiveValues" +
                    mpsFile.getName().substring(0, mpsFile.getName().length() - 4) + ".csv"), '\t');
            csvWriter.writeNext("Time", "Total Objective value", "Number of violated constraints", "Objective value");
            solver.solve(runtime);
            solver.printSolution("doc/output/L=25000_K=10/solutions/" +
                    mpsFile.getName().substring(0, mpsFile.getName().length() - 4) + ".txt");
            csvWriter.close();

            crossConstraintParam = 20;

            solver = new Solver(new MPSReader(), minimize, lateAcceptanceFitnessSize, crossConstraintParam, conflictGraphDepth, orderedCyclicShift, unorderedCyclicShift);
            solver.readProblem(mpsFile);
            csvWriter = new CSVWriter(new FileWriter("doc/output/L=25000_K=20/objectiveValues" +
                    mpsFile.getName().substring(0, mpsFile.getName().length() - 4) + ".csv"), '\t');
            csvWriter.writeNext("Time", "Total Objective value", "Number of violated constraints", "Objective value");
            solver.solve(runtime);
            solver.printSolution("doc/output/L=25000_K=20/solutions/" +
                    mpsFile.getName().substring(0, mpsFile.getName().length() - 4) + ".txt");
            csvWriter.close();
        }
    }
}
