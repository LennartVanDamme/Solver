package main;

import io.MPSReader;
import core.Solver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;

public class Main {

    public static final int GROUPSIZE = 2;
    public static long LOG_PERIOD = 2000;
    private static FileHandler fh;
    public static String outputDirectory;
    private static Map<String, Integer> fitnessArrayParams = new HashMap<>();

    public static void main(String[] args) throws Exception {

        fitnessArrayParams.put("acc-tight5.mps", 12000);
        fitnessArrayParams.put("air04.mps", 6000);
        fitnessArrayParams.put("ash608gpia-3col.mps", 350);
        fitnessArrayParams.put("bab5.mps", 600);
        fitnessArrayParams.put("bnatt350.mps", 4000);
        fitnessArrayParams.put("eilB101.mps",  3000);
        fitnessArrayParams.put("iis-100-0-cov.mps", 6500);
        fitnessArrayParams.put("m100n500k4r1.mps", 50000);
        fitnessArrayParams.put("mine-90-10.mps", 2500);
        fitnessArrayParams.put("neos-1337307.mps", 100);
        fitnessArrayParams.put("ns1688347.mps", 25000);
        fitnessArrayParams.put("reblock67.mps", 7000);

        final boolean minimize = true;
        final long runtime = 20 * 60;
        int lateAcceptanceFitnessSize;
        int crossConstraintParam = 0;
        final int conflictGraphDepth = 5;
        final boolean cyclicShift = false;

        File directory = new File(args[0]);


        for (File mpsFile : directory.listFiles()) {

            double temp = fitnessArrayParams.get(mpsFile.getName()) * 12 * runtime * 0.1;
            lateAcceptanceFitnessSize = (int) temp;

            Solver solver = solver = new Solver(new MPSReader(),
                    minimize, lateAcceptanceFitnessSize, crossConstraintParam, conflictGraphDepth, cyclicShift);
            System.out.println("READING PROBLEM: " + mpsFile.getName());
            solver.readProblem(mpsFile);
            System.out.println("Solving");
            outputDirectory = args[1] + "K=10/";
            solver.solve(runtime);
            System.out.println("Done solving");
            System.out.println("Writing solution");
            solver.printSolution(outputDirectory+"solutions/" + mpsFile.getName().substring(0, mpsFile.getName().length() - 4) + ".txt");

            crossConstraintParam = 20;

            solver = new Solver(new MPSReader(), minimize, lateAcceptanceFitnessSize, crossConstraintParam, conflictGraphDepth, cyclicShift);
            System.out.println("READING PROBLEM: " + mpsFile.getName());
            solver.readProblem(mpsFile);
            System.out.println("Solving");
            outputDirectory = args[1] + "K=20/";
            solver.solve(runtime);
            System.out.println("Done solving");
            System.out.println("Writing solution");
            solver.printSolution(outputDirectory+"solutions/" +
                    mpsFile.getName().substring(0, mpsFile.getName().length() - 4) + ".txt");

            crossConstraintParam = 30;

            solver = new Solver(new MPSReader(), minimize, lateAcceptanceFitnessSize, crossConstraintParam, conflictGraphDepth, cyclicShift);
            System.out.println("READING PROBLEM: " + mpsFile.getName());
            solver.readProblem(mpsFile);
            System.out.println("Solving");
            outputDirectory = args[1] + "K=30/";
            solver.solve(runtime);
            System.out.println("Done solving");
            System.out.println("Writing solution");
            solver.printSolution(outputDirectory+"solutions/" +
                    mpsFile.getName().substring(0, mpsFile.getName().length() - 4) + ".txt");
        }
    }
}
