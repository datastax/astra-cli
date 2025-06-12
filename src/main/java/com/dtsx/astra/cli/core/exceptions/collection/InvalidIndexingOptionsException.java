package com.dtsx.astra.cli.core.exceptions.collection;

import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;

public class InvalidIndexingOptionsException extends OptionValidationException {
    public InvalidIndexingOptionsException() {
        super("indexing options", "indexing-allow and indexing-deny are mutually exclusive");
    }
}