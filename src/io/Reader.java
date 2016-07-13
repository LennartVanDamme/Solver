package io;

import solver.Solver;

/**
 * Created by Lennart on 1/07/16.
 */
public interface Reader {

    /**
     * Implement specific method to read a problem. The problem is threated as a model in the solver.
     * This method should contain the specification how constraints are read, how the objective is read and how the decision variables are read.
     * This can be done by invoking the methods of the model object in the solver.
     *
     * @param inputFile File path to the input file.
     * @param solver    Used solver in program.
     */
    void readFile(String inputFile, Solver solver);

}
