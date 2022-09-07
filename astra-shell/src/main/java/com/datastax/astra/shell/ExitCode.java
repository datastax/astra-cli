package com.datastax.astra.shell;

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
    INVALID_PARAMETER(10),
    
    /** code. */
    NOT_FOUND(20),
    /** conflict. */
    
    CONFLICT(21),
    /** conflict. */
    ALREADY_EXIST(22),
    
    /** code. */
    CANNOT_CONNECT(30), 
    
    /** Internal error. */
    INTERNAL_ERROR(40);
    
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
    
    /**
     * Exit the prgram.
     */
    public void exit() {
        System.exit(code);
    }
     
    

}
