package exceptions;

public class BPReaderException extends BPException {

    public static final int DUPLICATE_CONSTRAINT = 0;
    public static final int NON_INTEGER_VARIABLE = 1;
    public static final int CONSTRAINT_NOT_FOUND = 2;
    public static final int DECISIONVARIABLE_NOT_FOUND = 3;
    public static final int UNKNOWN_SECTION_READ = 4;
    public static final int NO_MPS_FILE = 5;
    private static final long serialVersionUID = -8786139996702530641L;
    private static final String DUPLICATE_CONSTRAINT_MESSAGE = "There is a duplicate constraint";
    private static final String NON_INTEGER_VARIABLE_MESSAGE = "This file containts non-integer decision variables.";
    private static final String CONSTRAINT_NOT_FOUND_MESSAGE = "CONSTRAINT NOT FOUND";
    private static final String DECISIONVARIABLE_NOT_FOUND_MESSAGE = "DECISION VARIABLE NOT FOUND";
    private static final String UNKNOWN_SECTION_READ_MESSAGE = "Unknow section read";
    private static final String NO_MPS_FILE_MESSAGE = "The given file is no mps-file. Check file format.";
    private int line;
    private String info1;

    public BPReaderException(int errorCode, int line) {
        super(errorCode);
        this.line = line;
    }

    public BPReaderException(int errorCode, int line, String info1) {
        super(errorCode);
        this.line = line;
        this.info1 = info1;
    }


}
