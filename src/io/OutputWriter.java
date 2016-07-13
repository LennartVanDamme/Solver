package io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Lennart on 16/06/2016.
 */
public class OutputWriter {

    private static final String CONSTRAINTS_FILEPATH = "/doc/output/constraints.txt";
    private static final String DECISIONVARIABLES_FILEPATH = "/doc/output/decisionvariables.txt";
    private static final String SOLUTION_FILEPATH = "/doc/output/solution.txt";

    private String constraintFilePath;
    private String decisionvariablesFilepath;
    private String solutionFilepath;

    private boolean[] pathSet;

    private BufferedWriter writer;

    public OutputWriter() {
        pathSet = new boolean[3];
    }

    public void setConstraintsFilepath(String constraintFilePath) {
        this.constraintFilePath = constraintFilePath;
        pathSet[0] = true;
    }

    public void setDecisionvariablesFilepath(String decisionvariablesFilepath) {
        this.decisionvariablesFilepath = decisionvariablesFilepath;
        pathSet[1] = true;
    }

    public void setSolutionFilepath(String solutionFilepath) {
        this.solutionFilepath = solutionFilepath;
        pathSet[2] = true;
    }

    public void writeConstraints(String[] constraints) throws IOException {
        if (pathSet[0]) {
            writer = new BufferedWriter(new FileWriter(constraintFilePath));
        } else {
            writer = new BufferedWriter(new FileWriter(CONSTRAINTS_FILEPATH));
        }
        writer.write("CONSTRAINTS:" + System.lineSeparator() + System.lineSeparator());
        for (String constraint : constraints) {
            writer.write("    " + constraint + "    " + System.lineSeparator());
        }
        writer.close();
    }

    public void writeDecisionvariables(String[] decisionvariables) throws IOException {
        if (pathSet[0]) {
            writer = new BufferedWriter(new FileWriter(decisionvariablesFilepath));
        } else {
            writer = new BufferedWriter(new FileWriter(DECISIONVARIABLES_FILEPATH));
        }
        writer.write("DECISIONVARIABLES:" + System.lineSeparator() + System.lineSeparator());
        for (String decisionvariable : decisionvariables) {
            writer.write("    " + decisionvariable + "    " + System.lineSeparator());
        }
        writer.close();
    }


}
