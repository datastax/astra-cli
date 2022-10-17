package com.datastax.astra.cli.db.dsbulk;

/**
 * Configuration Bean.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public record DsBulkConfig(String url, String version) {}
