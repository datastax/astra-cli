package com.datastax.astra.shell.cmd;

import com.datastax.astra.shell.out.OutputFormat;
import com.github.rvesse.airline.annotations.Option;

/**
 * Options, parameters and treatments that you want to apply on all commands.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractCmd implements Runnable {
    
    /** Command constants. */
    public static final String CREATE     = "create";
    
    /** Command constants. */
    public static final String DELETE     = "delete";
    
    /** Command constants. */
    public static final String GET       = "get";
    
    /** Command constants. */
    public static final String LIST       = "list";
    
    /** Command constants. */
    public static final String USE       = "use";
    
    // --- Flags ---
    
    /** 
     * Each command can have a verbose mode. 
     **/
    @Option(name = { "-v","--verbose" }, description = "Verbose mode with log in console")
    protected boolean verbose = false;
    
    /** 
     * Each command can have a verbose mode. 
     **/
    @Option(name = { "--no-color" }, description = "Remove all colors in output")
    protected boolean noColor = false;
    
    /**
     * No log but provide output as a JSON
     */
    @Option(name = { "-o", "--output" }, 
            title = "FORMAT",
            description = "Output format, valid values are: human,json,csv")
    protected OutputFormat output = OutputFormat.human;
    
    /**
     * Getter accessor for attribute 'format'.
     *
     * @return
     *       current value of 'format'
     */
    public OutputFormat getOutput() {
        return output;
    }

    /**
     * Getter accessor for attribute 'debug'.
     *
     * @return
     *       current value of 'debug'
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Getter accessor for attribute 'noColor'.
     *
     * @return
     *       current value of 'noColor'
     */
    public boolean isNoColor() {
        return noColor;
    }
    

}
