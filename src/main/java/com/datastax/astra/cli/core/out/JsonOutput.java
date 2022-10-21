package com.datastax.astra.cli.core.out;

import com.datastax.astra.cli.core.ExitCode;

/**
 * Wrapper for Json outputs.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class JsonOutput<T> {
    
    /**
     * Returned code.
     */
    private int code = ExitCode.SUCCESS.getCode();
    
    /**
     * Returned message
     */
    private String message;
    
    /**
     * Custom payload
     */
    private T data;

    /**
     * Default constructor.
     */
    public JsonOutput() {}
    
    /**
     * Constructor with fields.
     *
     * @param code
     *      returned code
     * @param message
     *      returned message
     */
    public JsonOutput(ExitCode code, String message) {
        super();
        if (code != null) {
            this.code = code.getCode();
        }
        this.message = message;
    }

    /**
     * Constructor with fields.
     *
     * @param code
     *      returned code
     * @param message
     *      returned message
     * @param data
     *      data in JSON
     */
    public JsonOutput(ExitCode code, String message, T data) {
        this(code, message);
        this.data     = data;  
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
     * Setter accessor for attribute 'code'.
     * @param code
     * 		new value for 'code '
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Getter accessor for attribute 'message'.
     *
     * @return
     *       current value of 'message'
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter accessor for attribute 'message'.
     * @param message
     * 		new value for 'message '
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter accessor for attribute 'data'.
     *
     * @return
     *       current value of 'data'
     */
    public Object getData() {
        return data;
    }

    /**
     * Setter accessor for attribute 'data'.
     * @param data
     * 		new value for 'data '
     */
    public void setData(T data) {
        this.data = data;
    }
    

}
