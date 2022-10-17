package com.datastax.astra.cli.db.cqlsh;

/**
 * Load config from micronaut
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public record CqlShellConfig(String url, String tarball) {}

