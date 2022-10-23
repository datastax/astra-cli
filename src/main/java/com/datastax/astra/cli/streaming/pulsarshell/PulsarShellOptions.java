package com.datastax.astra.cli.streaming.pulsarshell;

/**
 * Options for Pulsar Shell CLI
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class PulsarShellOptions {

    /** option. */
    private String execute;
    
    /** option. */
    protected boolean failOnError = false;
    
    /** option. */
    protected String fileName;
    
    /** option. */
    protected boolean noProgress = false;

    /**
     * Getter accessor for attribute 'execute'.
     *
     * @return
     *       current value of 'execute'
     */
    public String getExecute() {
        return execute;
    }

    /**
     * Setter accessor for attribute 'execute'.
     * @param execute
     * 		new value for 'execute '
     */
    public void setExecute(String execute) {
        this.execute = execute;
    }

    /**
     * Getter accessor for attribute 'failOnError'.
     *
     * @return
     *       current value of 'failOnError'
     */
    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * Setter accessor for attribute 'failOnError'.
     * @param failOnError
     * 		new value for 'failOnError '
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Getter accessor for attribute 'fileName'.
     *
     * @return
     *       current value of 'fileName'
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter accessor for attribute 'fileName'.
     * @param fileName
     * 		new value for 'fileName '
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Getter accessor for attribute 'noProgress'.
     *
     * @return
     *       current value of 'noProgress'
     */
    public boolean isNoProgress() {
        return noProgress;
    }

    /**
     * Setter accessor for attribute 'noProgress'.
     * @param noProgress
     * 		new value for 'noProgress '
     */
    public void setNoProgress(boolean noProgress) {
        this.noProgress = noProgress;
    }
   
}
