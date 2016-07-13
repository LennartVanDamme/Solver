package io;

import exceptions.BPException;
import exceptions.BPReaderException;
import model.Constraint;
import model.DecisionVariable;
import model.ObjectiveFunction;
import model.SetPackingConstraint;
import solver.Solver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MPSReader implements Reader {

    public MPSReader() {
    }

    private static int checkSection(String input) {
        if (input.toUpperCase().contains("ROWS")) {
            System.out.println("Rows sectie");
            return 1;
        }
        if (input.toUpperCase().contains("COLUMNS")) {
            System.out.println("Columns sectie");
            return 2;
        }
        if (input.toUpperCase().contains("RHS")) {
            System.out.println("RHS sectie");
            return 3;
        }
        if (input.toUpperCase().contains("RANGES")) {
            System.out.println("ranges sectie");
            return 4;
        }
        if (input.toUpperCase().contains("BOUNDS")) {
            System.out.println("Bounds sectie");
            return 5;
        }
        if (input.toUpperCase().contains("ENDATA")) {
            System.out.println("End data");
            return 6;
        }
        return 0;
    }

    @Override
    public void readFile(String inputFileName, Solver solver) {

        File inputFile = new File(inputFileName);
        int line = 1;
        try {

            Scanner scanner = new Scanner(inputFile);
            Scanner filter;
            String input, decisionVarName, constraintName, type;
            double coefficient, rhs, r;
            int bound, constraintType;
            boolean objectiveRead = false;

            boolean marked = false;

            // Leest de naam van het probleem
            input = scanner.nextLine();
            solver.getModel().setName(input.substring(14).trim());
            int section = 1;

            while (scanner.hasNextLine() && !input.equals("ENDATA")) {
                input = scanner.nextLine();
                // System.out.println(input);
                filter = new Scanner(input);
                if (input.charAt(0) != ' ') {
                    if (input.charAt(0) == '%') {
                        scanner.nextLine();
                    } else {
                        section = checkSection(input);
                    }
                }
                /**
                 * Hier wordt de rows sectie ingelezen. Deze sectie bevat de
                 * naam van de constraints en van welk type ze zijn
                 *
                 */
                else if (section == 1) {

                    type = filter.next();
                    constraintName = filter.next();

                    // Als het objectief nog niet is ingelezen, wordt dit nu
                    // gedaan.
                    // Het objectief wordt verondersteld als eerste constraint
                    // te staan.
                    if (!objectiveRead) {
                        ObjectiveFunction objectiveFunction = new ObjectiveFunction(constraintName, Constraint.NON_RESTRICTIVE);
                        objectiveRead = true;
                        solver.getModel().setObjectiveFunction(objectiveFunction);

                    } else {
                        try {

                            // Elke constraint naam moet uniek zijn, dus als er
                            // eentje tweemaal optreed dan levert dit een fout
                            // op
                            if (solver.getModel().containsConstraint(constraintName)) {
                                throw new BPReaderException(BPReaderException.DUPLICATE_CONSTRAINT, line,
                                        constraintName);
                            } else {
                                constraintType = Solver.constraintTypeConverter(type);
                                if (constraintType == Constraint.SET_PACKING_CONSTRAINT) {
                                    solver.getModel().addConstraint(new SetPackingConstraint(constraintName, constraintType));
                                    solver.getModel().addSetPackingConstraint(constraintName);
                                } else {
                                    solver.getModel().addConstraint(new Constraint(constraintName, constraintType));
                                }
                            }
                        } catch (BPReaderException e) {
                            break;
                        }

                    }
                    /**
                     * Hier wordt de colums sectie ingelezen, deze bevat de
                     * waarden van de coefficienten van elke constraint.
                     *
                     */
                } else if (section == 2) {

                    try {
                        if (input.toUpperCase().contains("INTORG")) {
                            marked = true;
                        } else if (input.toUpperCase().contains("INTEND")) {
                            marked = false;
                        } else if (!marked) {
                            throw new BPReaderException(BPReaderException.NON_INTEGER_VARIABLE, line);
                        } else {

                            decisionVarName = filter.next();
                            constraintName = filter.next();
                            if (!constraintName.equals(solver.getModel().getObjectiveFunction().getName())) {
                                constraintType = solver.getModel().getContraint(constraintName).getType();
                            } else {
                                constraintType = Constraint.OBJECTIVE;
                            }

                            coefficient = Double.parseDouble(filter.next());

                            if (!solver.getSolution().getDecisionVariableMap().containsKey(decisionVarName)) {
                                solver.getSolution().getDecisionVariableMap().put(decisionVarName, new DecisionVariable(decisionVarName));
                            }

                            if (!solver.getModel().containsConstraint(constraintName) && !solver.getModel().getObjectiveFunction().getName().equals(constraintName)) {
                                throw new BPReaderException(BPReaderException.CONSTRAINT_NOT_FOUND, line, constraintName);
                            }

                            if (constraintType == Constraint.OBJECTIVE) {
                                solver.getModel().getObjectiveFunction().addCoefficient(decisionVarName, coefficient);
                                solver.getSolution().getDecisionVariableMap().get(decisionVarName).setInObjective(true);
                            } else {
                                solver.getModel().addCoeffientToConstraint(constraintName, decisionVarName, coefficient);
                                solver.getSolution().getDecisionVariableMap().get(decisionVarName).getConstraints().add(constraintName);
                            }

                            if (filter.hasNext()) {

                                constraintName = filter.next();
                                coefficient = Double.parseDouble(filter.next());
                                if (!constraintName.equals(solver.getModel().getObjectiveFunction().getName())) {
                                    constraintType = solver.getModel().getContraint(constraintName).getType();
                                } else {
                                    constraintType = Constraint.OBJECTIVE;
                                }

                                if (!solver.getModel().containsConstraint(constraintName) && !solver.getModel().getObjectiveFunction().getName().equals(constraintName)) {
                                    throw new BPReaderException(BPReaderException.CONSTRAINT_NOT_FOUND, line, constraintName);
                                }


                                if (constraintType == Constraint.OBJECTIVE) {
                                    solver.getModel().getObjectiveFunction().addCoefficient(decisionVarName, coefficient);
                                    solver.getSolution().getDecisionVariableMap().get(decisionVarName).setInObjective(true);
                                } else {
                                    solver.getModel().addCoeffientToConstraint(constraintName, decisionVarName, coefficient);
                                    solver.getSolution().getDecisionVariableMap().get(decisionVarName).getConstraints().add(constraintName);
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        break;
                    } catch (BPReaderException e) {
                        break;
                    }

                    /**
                     * Inlezen van de RHS sectie
                     *
                     */
                } else if (section == 3) {

                    // Verwijderen van de RHS naam
                    filter.next();

                    try {

                        constraintName = filter.next();
                        rhs = Double.parseDouble(filter.next());

                        if (!solver.getModel().containsConstraint(constraintName)) {
                            throw new BPReaderException(BPReaderException.CONSTRAINT_NOT_FOUND, line, constraintName);
                        } else {
                            solver.getModel().getContraint(constraintName).setRightHandSide(rhs);
                        }

                        if (filter.hasNext()) {
                            constraintName = filter.next();
                            rhs = Double.parseDouble(filter.next());
                            if (!solver.getModel().containsConstraint(constraintName)) {
                                throw new BPReaderException(BPReaderException.CONSTRAINT_NOT_FOUND, line,
                                        constraintName);
                            } else {
                                solver.getModel().getContraint(constraintName).setRightHandSide(rhs);
                            }
                        }

                    } catch (NumberFormatException e) {
                        break;
                    } catch (BPReaderException e) {

                        break;
                    }
                    /*
                     * Inlezen van de RANGES sectie
					 *
					 */
                } else if (section == 4) {

                    // Verwijderen van RANGE naam
                    filter.next();

                    try {

                        constraintName = filter.next();
                        r = Double.parseDouble(filter.next());

                        if (!solver.getModel().containsConstraint(constraintName)) {
                            throw new BPReaderException(BPReaderException.CONSTRAINT_NOT_FOUND, line, constraintName);
                        } else {
                            solver.getModel().getContraint(constraintName).calculateRange(r);
                        }

                        if (filter.hasNext()) {

                            constraintName = filter.next();
                            r = Double.parseDouble(filter.next());

                            if (!solver.getModel().containsConstraint(constraintName)) {
                                throw new BPReaderException(BPReaderException.CONSTRAINT_NOT_FOUND, line);
                            } else {
                                solver.getModel().getContraint(constraintName).calculateRange(r);
                            }
                        }
                    } catch (NumberFormatException e) {
                        break;
                    } catch (BPReaderException e) {
                        break;
                    }

					/*
                     * Inlezen van BOUNDS section
					 */
                } else if (section == 5) {

                    type = filter.next();
                    filter.next();
                    decisionVarName = filter.next();
                    bound = Integer.parseInt(filter.next());

                    try {
                        if (!solver.getSolution().getDecisionVariableMap().containsKey(decisionVarName)) {
                            throw new BPReaderException(BPReaderException.DECISIONVARIABLE_NOT_FOUND, line,
                                    decisionVarName);
                        } else {
                            try {
                                solver.getSolution().getDecisionVariableMap().get(decisionVarName).setBound(bound);
                                solver.getSolution().getDecisionVariableMap().get(decisionVarName).setBoundType(type.toUpperCase());
                                if (!solver.getSolution().getDecisionVariableMap().get(decisionVarName).checkBinary()) {
                                    BPException bpException = new BPException(BPException.DECISION_VARIABLE_NOT_BINARY);
                                    bpException.setMessage(decisionVarName);
                                    throw bpException;
                                }
                            } catch (BPException e) {
                                break;
                            }
                        }
                    } catch (BPReaderException e) {
                        break;
                    }

                } else if (section == 0) {
                    try {
                        System.out.println("ERROR");
                        throw new BPReaderException(BPReaderException.UNKNOWN_SECTION_READ, line);
                    } catch (BPReaderException e) {
                        break;
                    }
                }
                line++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
    }
}
