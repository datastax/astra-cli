package com.datastax.astra.shell.out;

/**
 * Output of the commands.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public enum OutputFormat {
    
    /** Output as a JSON. */
    json,
    
    /** Output as tables. */
    human,
    
    /** Output as CSV. */
    csv;

}
