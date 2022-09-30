package com.datastax.astra.cli;

/**
 * Normalization of exit codes.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public enum ExitCode {
    
    /** code ok. */
    SUCCESS(0),
    
    /** code. */
    PARSE_ERROR(1),
    
    /** code. */
    UNAVAILABLE(2),
    
    /** code. */
    NOT_IMPLEMENTED(3),
    
    /** code. */
    INVALID_PARAMETER(4),
    
    /** code. */
    NOT_FOUND(5),
    
    /** conflict. */
    CONFLICT(6),
    
    /** conflict. */
    ALREADY_EXIST(7),
    
    /** code. */
    CANNOT_CONNECT(8), 
    
    /** code. */
    CONFIGURATION(9), 
    
    /** code. */
    ILLEGAL_STATE(10), 
    
    /** code. */
    INVALID_ARGUMENT(11),
    
    /** code. */
    INVALID_OPTION(12),
    
    /** code. */
    INVALID_OPTION_VALUE(13),
    
    /** code. */
    UNRECOGNIZED_COMMAND(14),
    
    /** Internal error. */
    INTERNAL_ERROR(100);
    
    /* Exit code. */
    private int code;
    
    /**
     * Constructor.
     *
     * @param code
     *      target code
     */
    private ExitCode(int code) {
        this.code = code;
    }

    /**
     * Getter accessor for attribute 'code'.
     *
     * @return
     *       current value of 'code'
     */
    public int getCode() {
        return code;
    }

}
