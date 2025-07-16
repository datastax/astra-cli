package com.dtsx.astra.cli.core.output.select;

import com.dtsx.astra.cli.core.datatypes.NEList;

import java.util.Optional;
import java.util.function.Function;

public interface SelectionStrategy<T> {
    Optional<T> select();

    record SelectionRequest<T>(
        String prompt,
        NEList<String> options,
        Optional<String> defaultOption,
        Function<String, T> mapper,
        boolean clearAfterSelection
    ) {}

    interface Meta {
        boolean isSupported();
        <T> SelectionStrategy<T> mkInstance(SelectionRequest<T> request);
    }
}
