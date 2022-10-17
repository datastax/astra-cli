package com.datastax.astra.cli.db.exception;

import com.datastax.astra.cli.core.out.LoggerShell;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabaseNotSelectedException extends RuntimeException {

    /** Serial Number. */
    private static final long serialVersionUID = 8155558354861561721L;
    
    /**
     * Default constructor
     */
    public DatabaseNotSelectedException() {
        super("This command requires you to select a database first with db use");
        LoggerShell.error("You must select a DB first with 'db use <dbname>'");
    }

}
