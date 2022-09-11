package com.datastax.astra.cli.db.cqlsh;

import java.io.Serializable;

/**
 * Hold options for CqlShell.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class CqlShellOptions implements Serializable {
    
    /** Serial. */
    private static final long serialVersionUID = 2325445470242154158L;

    /** Cqlsh Options. */
    private boolean version = false;
    
    /** Cqlsh Options. */
    private boolean debug = false;
    
    /** Cqlsh Options. */
    private String encoding;
    
    /** Cqlsh Options. */
    private String execute;
    
    /** Cqlsh Options. */
    private String file;
    
    /** Cqlsh Options. */
    private String keyspace;

    /**
     * Getter accessor for attribute 'version'.
     *
     * @return
     *       current value of 'version'
     */
    public boolean isVersion() {
        return version;
    }

    /**
     * Setter accessor for attribute 'version'.
     * @param version
     * 		new value for 'version '
     */
    public void setVersion(boolean version) {
        this.version = version;
    }

    /**
     * Getter accessor for attribute 'debug'.
     *
     * @return
     *       current value of 'debug'
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Setter accessor for attribute 'debug'.
     * @param debug
     * 		new value for 'debug '
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Getter accessor for attribute 'encoding'.
     *
     * @return
     *       current value of 'encoding'
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Setter accessor for attribute 'encoding'.
     * @param encoding
     * 		new value for 'encoding '
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

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
     * Getter accessor for attribute 'file'.
     *
     * @return
     *       current value of 'file'
     */
    public String getFile() {
        return file;
    }

    /**
     * Setter accessor for attribute 'file'.
     * @param file
     * 		new value for 'file '
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Getter accessor for attribute 'keyspace'.
     *
     * @return
     *       current value of 'keyspace'
     */
    public String getKeyspace() {
        return keyspace;
    }

    /**
     * Setter accessor for attribute 'keyspace'.
     * @param keyspace
     * 		new value for 'keyspace '
     */
    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

}
