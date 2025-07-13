package com.dtsx.astra.cli.core.output.select;

import com.dtsx.astra.cli.core.datatypes.NEList;

import java.util.Optional;
import java.util.function.Function;

public interface SelectionStrategy<T> {
    SelectStatus<T> select();
    SelectStatus<T> select(boolean clearAfterSelection);

    interface Meta {
        boolean isSupported();
        <T> SelectionStrategy<T> mkInstance(String prompt, NEList<String> options, Optional<String> defaultOption, Function<String, T> mapper);
    }
}
