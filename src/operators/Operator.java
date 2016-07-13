package operators;

import model.Solution;

public interface Operator {


    /**
     * Performs the specified moved.
     */
    void doMove(Solution solution);

    /**
     * Undoes the performed move, returning to the original state
     */
    void undoMove(Solution solution);


}
