package com.datastax.astra.cli.db.cqlsh;

/**
 * Record storing options.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public record CqlShellOption(boolean version, boolean debug, String encoding, String execute, String file, String keyspace) {
}
