package com.datastax.astra.cli.core.out;

/**
 * Output of the commands.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public enum OutputFormat {
    
    /** Output as a JSON. */
    JSON,
    
    /** Output as tables. */
    HUMAN,
    
    /** Output as comma. */
    CSV

}
