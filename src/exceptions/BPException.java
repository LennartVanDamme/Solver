package exceptions;

/**
 * Exceptions raised when checking a model are defined here.
 *
 * @author Lennart
 */
public class BPException extends Exception {

    public static final int DECISION_VARIABLE_NOT_BINARY = 0;
    public static final int DECISION_VARIABLES_EMPTY = 1;
    public static final int CONSTRAINTS_EMPTY = 2;
    public static final int OBJECTIVE_EMPTY = 3;
    public static final int SET_COVERING_CONSTRAINT_CANT_BE_SATISFIED = 4;
    public static final int SET_PACKING_CONSTRAINT_CANT_BE_SATISFIED = 5;
    public static final int EQUILITY_CONSTRAINT_CANT_BE_SATISFIED = 6;
    protected static final String DEFAULT_EXCEPTION_MESSAGE = "There is an exception but I don't know which one.";
    private static final long serialVersionUID = 9002L;
    private static final String DECISION_VARIABLE_NOT_BINARY_MESSGAE = "A decision variable is not binary";
    private static final String DECISION_VARIABLES_EMPTY_MESSAGE = "There are no decision variables in the model.";
    private static final String CONSTRAINTS_EMPTY_MESSGAGE = "There are no constraints in the model.";
    private static final String OBJECTIVE_EMPTY_MESSAGE = "The objective funtion is empty.";
    private static final String SET_COVERING_CONSTRAINT_CANT_BE_SATISFIED_MESSAGE = "Set covering constraint is violated";
    private static final String SET_PACKING_CONSTRAINT_CANT_BE_SATISFIED_MESSAGE = "Set packing constraint is violated";
    private static final String EQUILITY_CONSTRAINT_CANT_BE_SATISFIED_MESSAGE = "Equility constraint is violated";
    private int exceptionCode;
    private String message;

    public BPException() {
        // TODO Auto-generated constructor stub
    }

    public BPException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public BPException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public BPException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public BPException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public BPException(int exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
