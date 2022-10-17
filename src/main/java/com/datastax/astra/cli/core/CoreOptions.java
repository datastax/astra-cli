package com.datastax.astra.cli.core;

import com.datastax.astra.cli.core.out.OutputFormat;

/**
 * User options.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public record CoreOptions(
        boolean verbose, 
        boolean noColor, 
        OutputFormat output, 
        String configFilename) {}
